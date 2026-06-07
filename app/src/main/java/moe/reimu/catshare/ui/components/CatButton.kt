package moe.reimu.catshare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.reimu.catshare.ui.theme.CatShareShapes

// ===== 按钮变体 =====
enum class CatButtonVariant { Filled, Tonal, Outlined, Text }

// ===== 通用按钮（多态） =====
@Composable
fun CatButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CatButtonVariant = CatButtonVariant.Filled,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val content: @Composable RowScope.() -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }

    when (variant) {
        CatButtonVariant.Filled -> {
            Button(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                enabled = enabled,
                shape = CatShareShapes.medium,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                content = content,
            )
        }

        CatButtonVariant.Tonal -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                enabled = enabled,
                shape = CatShareShapes.medium,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                content = content,
            )
        }

        CatButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                enabled = enabled,
                shape = CatShareShapes.medium,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                content = content,
            )
        }

        CatButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                enabled = enabled,
                shape = CatShareShapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                content = content,
            )
        }
    }
}

// ===== 主操作按钮 — 带成功态动画 =====
@Composable
fun CatPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    success: Boolean = false,
) {
    var isSuccess by remember { mutableStateOf(success) }
    val containerColor: Color by animateColorAsState(
        targetValue = if (isSuccess) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.primary,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "primary_btn_color",
    )

    Button(
        onClick = {
            if (enabled) {
                onClick()
                if (success) {
                    isSuccess = true
                }
            }
        },
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        enabled = enabled,
        shape = CatShareShapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColorFor(containerColor),
        ),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
    ) {
        AnimatedVisibility(
            visible = isSuccess,
            enter = scaleIn(animationSpec = spring()),
            exit = scaleOut(animationSpec = spring()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✓ ",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
