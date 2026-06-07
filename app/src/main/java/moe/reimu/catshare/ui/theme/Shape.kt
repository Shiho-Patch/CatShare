package moe.reimu.catshare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// M3 Expressive 形状系统 — 更具表现力的圆角
val CatShareShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

// 额外的常用形状
val PillShape = RoundedCornerShape(percent = 50)
val TopLargeShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
