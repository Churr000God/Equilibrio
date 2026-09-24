package mx.equilibrio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.R
import mx.equilibrio.ui.theme.EquilibrioTheme

/**
 * Logo de marca en cristal (triángulo = gastos, esfera = ingresos, el cruce = el equilibrio).
 * La variante clara/oscura la elige Android por `drawable-night-nodpi`, igual que el theme
 * (que sigue a `isSystemInDarkTheme`). Pensado para ≥48dp: más chico, el cristal no se lee.
 */
@Composable
fun EqLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    contentDescription: String? = "Equilibrio",
) {
    Image(
        painter = painterResource(R.drawable.eq_logo_glass),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
    )
}

/** Ilustraciones de marca para [EqEmptyState]. Decorativas: sin contentDescription. */
enum class EqIllustration {
    /** Solo la esfera: "aún no hay nada que equilibrar" (sin movimientos, sin metas). */
    BALANCE,

    /** Logo en gris: sin datos que mostrar todavía (reportes, categorías, búsqueda). */
    NEUTRAL,
}

@Composable
internal fun EqIllustrationImage(illustration: EqIllustration, modifier: Modifier = Modifier) {
    val res = when (illustration) {
        EqIllustration.BALANCE -> R.drawable.eq_empty_balance
        EqIllustration.NEUTRAL -> R.drawable.eq_empty_neutral
    }
    Image(painter = painterResource(res), contentDescription = null, modifier = modifier)
}

/** Sello Premium: el logo con brillo de bordes sobre su medallón oscuro (igual en ambos temas). */
@Composable
fun EqPremiumBadge(modifier: Modifier = Modifier, size: Dp = 72.dp) {
    Image(
        painter = painterResource(R.drawable.eq_premium_badge),
        contentDescription = null,
        modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.28f)),
    )
}

// Geometría del logo plano (assets_para_el_logo/asset-3.svg y asset_4.svg), en su viewBox de 1000.
private const val MARK_LEFT = 122f
private const val MARK_TOP = 182f
private const val MARK_WIDTH = 756f
private const val MARK_HEIGHT = 636f

/**
 * Silueta de un color del logo (asset 4): triángulo + esfera separados por un corte fino.
 * Se dibuja, no es imagen, así que se tiñe y escala sin perder nitidez — pensado para
 * marcas chicas sobre fondos de color (p. ej. la esquina de [EqAccountCard]).
 * [width] define el tamaño; la altura sale de la proporción del logo.
 */
@Composable
fun EqLogoMark(
    color: Color,
    modifier: Modifier = Modifier,
    width: Dp = 32.dp,
) {
    Canvas(
        modifier = modifier
            .size(width = width, height = width * (MARK_HEIGHT / MARK_WIDTH))
            // Capa propia: el corte (BlendMode.Clear) solo debe borrar el triángulo, no la tarjeta de
            // abajo. El alfa va en la capa: relleno y trazo del triángulo se enciman y, con un color
            // semitransparente, el borde saldría más opaco que el centro.
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                alpha = color.alpha
            },
    ) {
        val solid = color.copy(alpha = 1f)
        val scale = size.width / MARK_WIDTH
        withTransform({
            scale(scale, scale, pivot = Offset.Zero)
            translate(-MARK_LEFT, -MARK_TOP)
        }) {
            val triangle = Path().apply {
                moveTo(398f, 362f)
                lineTo(158f, 782f)
                lineTo(638f, 782f)
                close()
            }
            drawPath(triangle, solid, style = Fill)
            drawPath(triangle, solid, style = Stroke(width = 72f, join = StrokeJoin.Round))
            drawCircle(Color.Black, radius = 262f, center = Offset(638f, 422f), blendMode = BlendMode.Clear)
            drawCircle(solid, radius = 240f, center = Offset(638f, 422f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqLogoMarkPreview() {
    EquilibrioTheme { EqLogoMark(color = EquilibrioTheme.colors.ink, width = 96.dp) }
}

@Preview(showBackground = true)
@Composable
private fun EqLogoPreview() {
    EquilibrioTheme { EqLogo() }
}

@Preview(showBackground = true)
@Composable
private fun EqPremiumBadgePreview() {
    EquilibrioTheme { EqPremiumBadge() }
}
