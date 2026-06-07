package moe.reimu.catshare.ui.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import moe.reimu.catshare.models.DiscoveredDevice
import moe.reimu.catshare.utils.BleUtils
import moe.reimu.catshare.utils.DeviceUtils

data class ShareUiState(
    val devices: List<DiscoveredDevice> = emptyList(),
    val isScanning: Boolean = false,
    val errorMessage: String? = null,
)

class ShareViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ShareUiState(),
    )

    private var scanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    fun startScan(context: Context) {
        if (_uiState.value.isScanning) return
        val btManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = btManager.adapter ?: return

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val record = result?.scanRecord ?: return
                // 解析广播数据
                var deviceName: String? = null
                var brandId: Byte? = null

                for ((uuid, data) in record.serviceData.entries) {
                    when (data.size) {
                        6 -> {
                            // 品牌 + 5GHz 标志位
                            val buf = java.nio.ByteBuffer.allocate(16)
                            buf.putLong(uuid.uuid.mostSignificantBits)
                            buf.putLong(uuid.uuid.leastSignificantBits)
                            brandId = buf.array()[3]
                        }
                        27 -> {
                            // 设备名称 + senderId
                            val nameBuf = mutableListOf<Byte>()
                            for (i in 10..25) {
                                if (data[i].toInt() != 0) {
                                    nameBuf.add(data[i])
                                } else {
                                    break
                                }
                            }
                            val senderIdRaw = data[8].toInt().shl(8).or(data[9].toInt())
                            val senderId = String.format("%04x", senderIdRaw)
                            var name = nameBuf.toByteArray().decodeToString()
                            if (name.lastOrNull() == '\t') {
                                name = name.removeSuffix("\t") + "..."
                            }
                            deviceName = name
                            val brand = brandId?.let { DeviceUtils.deviceNameById(it) }
                            val newDevice = DiscoveredDevice(
                                device = result.device,
                                id = senderId,
                                name = deviceName,
                                brand = brand,
                                supports5Ghz = false,
                            )
                            addOrReplace(newDevice)
                        }
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Scan failed (code=$errorCode)",
                        isScanning = false,
                    )
                }
            }
        }

        try {
            val filters = listOf(
                ScanFilter.Builder().setServiceUuid(ParcelUuid(BleUtils.ADV_SERVICE_UUID)).build()
            )
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            adapter.bluetoothLeScanner.startScan(filters, settings, callback)
            scanner = adapter.bluetoothLeScanner
            scanCallback = callback
            _uiState.update { it.copy(isScanning = true, errorMessage = null) }
        } catch (e: SecurityException) {
            _uiState.update {
                it.copy(errorMessage = "Bluetooth permission denied: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            val cb = scanCallback ?: return
            scanner?.stopScan(cb)
        } catch (_: Exception) {
            // 忽略异常：权限/状态问题不影响 UI
        }
        scanner = null
        scanCallback = null
        _uiState.update { it.copy(isScanning = false) }
    }

    private fun addOrReplace(device: DiscoveredDevice) {
        _uiState.update { state ->
            val replaced = state.devices.map {
                if (it.id == device.id) device else it
            }
            val newList = if (replaced.size == state.devices.size &&
                replaced.none { it.id == device.id }
            ) {
                state.devices + device
            } else {
                replaced
            }
            state.copy(devices = newList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}
