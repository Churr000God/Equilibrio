package mx.equilibrio.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing

/**
 * Ninguna curva con rebote en todo el sistema (§09): el trabajo de esta app
 * es bajar la ansiedad, no generar urgencia. `spring()` con
 * `dampingRatio < 1f` está prohibido.
 */
object Motion {
    val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasized: Easing = CubicBezierEasing(0.3f, 0f, 0.2f, 1f)
    val gaugeEasing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val linear: Easing = LinearEasing

    const val instant = 100
    const val fast = 160
    const val base = 220
    const val slow = 320
    const val gaugeFill = 700
}
