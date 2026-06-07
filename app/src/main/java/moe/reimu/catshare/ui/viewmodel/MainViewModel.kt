package moe.reimu.catshare.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import moe.reimu.catshare.services.GattServerService
import moe.reimu.catshare.utils.ServiceState
import moe.reimu.catshare.utils.registerInternalBroadcastReceiver

// ===== 主界面 UI 状态 =====
data class MainUiState(
    val isReceiverRunning: Boolean = false,
    val shizukuGranted: Boolean = false,
    val shizukuAvailable: Boolean = false,
    val localMacAddressGranted: Boolean = false,
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // ===== 内部状态接收器（Broadcast → Flow 桥接） =====
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ServiceState.ACTION_UPDATE_RECEIVER_STATE -> {
                    _uiState.update {
                        it.copy(isReceiverRunning = intent.getBooleanExtra("isRunning", false))
                    }
                }
            }
        }
    }

    private var receiverRegistered = false
    private var registeredContext: Context? = null

    // ===== 生命周期入口 =====
    fun attach(context: Context) {
        if (!receiverRegistered) {
            context.registerInternalBroadcastReceiver(
                receiver,
                IntentFilter(ServiceState.ACTION_UPDATE_RECEIVER_STATE),
            )
            receiverRegistered = true
            registeredContext = context
        }
        context.sendBroadcast(ServiceState.getQueryIntent())
    }

    private fun unregister() {
        if (receiverRegistered) {
            try {
                registeredContext?.unregisterReceiver(receiver)
            } catch (_: Throwable) {
                // 忽略重复 unregister
            }
            receiverRegistered = false
            registeredContext = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregister()
    }

    // ===== Shizuku 状态 =====
    fun updateShizukuState(available: Boolean, granted: Boolean) {
        _uiState.update {
            it.copy(
                shizukuAvailable = available,
                shizukuGranted = granted,
            )
        }
    }

    fun setLocalMacAddressGranted(granted: Boolean) {
        _uiState.update { it.copy(localMacAddressGranted = granted) }
    }

    // ===== 业务操作：切换可见开关 =====
    fun toggleReceiver(context: Context, targetOn: Boolean) {
        if (targetOn) {
            GattServerService.start(context)
        } else {
            GattServerService.stop(context)
        }
    }
}
