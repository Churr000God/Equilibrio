package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioColors
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing

/**
 * Reglas de relleno (§10, tabla de insignias):
 * - Solid → color de dominio sólido, texto/ícono blanco.
 * - Soft → relleno suave, texto siempre en el tono profundo (nunca el tono
 *   base: `green` sobre `greenSoft` falla contraste AA de cuerpo).
 * - Un movimiento sin clasificar (`tone = NEUTRAL`) siempre usa el relleno
 *   neutro, sin importar `solid` — invariante I3: nunca recibe verde/morado
 *   hasta que el usuario lo clasifica.
 */
@Composable
fun EqBadge(
    text: String,
    tone: DomainTone,
    modifier: Modifier = Modifier,
    solid: Boolean = false,
) {
    val colors = EquilibrioTheme.colors
    val (background, foreground) = badgeColors(tone, solid, colors)

    Text(
        text = text,
        style = EquilibrioTheme.typography.label,
        color = foreground,
        modifier = modifier
            .background(background, ShapePill)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    )
}

private fun badgeColors(tone: DomainTone, solid: Boolean, colors: EquilibrioColors): Pair<Color, Color> {
    if (tone == DomainTone.NEUTRAL) return colors.neutralBadge to colors.inkMuted

    val (base, deep, soft) = when (tone) {
        DomainTone.INCOME_FIXED -> Triple(colors.greenDeep, colors.greenDeep, colors.greenSoft)
        DomainTone.INCOME_VARIABLE -> Triple(colors.greenMid, colors.greenDeep, colors.greenSoft)
        DomainTone.ESSENTIAL -> Triple(colors.green, colors.greenDeep, colors.greenSoft)
        DomainTone.RECREATIONAL -> Triple(colors.purple, colors.purpleDeep, colors.purpleSoft)
        DomainTone.NEUTRAL -> return colors.neutralBadge to colors.inkMuted
    }

    return if (solid) base to Color.White else soft to deep
}

@Preview(showBackground = true)
@Composable
private fun EqBadgePreview() {
    EquilibrioTheme {
        Column(
            modifier = Modifier.padding(Spacing.base),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            EqBadge("Fijo", DomainTone.INCOME_FIXED)
            EqBadge("Variable", DomainTone.INCOME_VARIABLE)
            EqBadge("Esencial", DomainTone.ESSENTIAL)
            EqBadge("Recreativo", DomainTone.RECREATIONAL, solid = true)
            EqBadge("Sin clasificar", DomainTone.NEUTRAL)
        }
    }
}
