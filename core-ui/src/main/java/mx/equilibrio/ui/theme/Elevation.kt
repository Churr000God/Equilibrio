package mx.equilibrio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

/**
 * Tres niveles de elevación. La sombra siempre usa el color de tinta a baja
 * opacidad — nunca negro puro, nunca un color de dominio o de estado (§08).
 */
enum class Elevation(val dp: androidx.compose.ui.unit.Dp, val alpha: Float) {
    LEVEL_0(0.dp, 0f),
    LEVEL_1(4.dp, 0.05f),
    LEVEL_2(12.dp, 0.08f),
    LEVEL_3(16.dp, 0.10f),
}

@Composable
fun Modifier.eqShadow(level: Elevation, shape: RoundedCornerShape = ShapeMedium): Modifier {
    val tint = EquilibrioTheme.colors.ink.copy(alpha = level.alpha)
    return if (level == Elevation.LEVEL_0) {
        this
    } else {
        shadow(elevation = level.dp, shape = shape, ambientColor = tint, spotColor = tint)
    }
}
