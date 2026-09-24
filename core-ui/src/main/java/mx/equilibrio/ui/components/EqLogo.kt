package mx.equilibrio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
