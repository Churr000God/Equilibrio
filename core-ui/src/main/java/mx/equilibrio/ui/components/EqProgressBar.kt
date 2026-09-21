package mx.equilibrio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Motion
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.color

/** Barra lineal de avance o proporción. Relleno siempre de dominio (I2). */
@Composable
fun EqProgressBar(
    progress: Float,
    tone: DomainTone,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = Motion.slow, easing = Motion.standard),
        label = "barProgress",
    )
    val colors = EquilibrioTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(ShapePill)
            .background(colors.border),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animated)
                .fillMaxHeight()
                .clip(ShapePill)
                .background(tone.color(colors)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EqProgressBarPreview() {
    EquilibrioTheme {
        Box(modifier = Modifier.padding(Spacing.base)) {
            EqProgressBar(progress = 0.4f, tone = DomainTone.RECREATIONAL)
        }
    }
}
