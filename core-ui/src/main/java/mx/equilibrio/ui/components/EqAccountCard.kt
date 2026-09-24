package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioColors
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeLarge
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.eqShadow
import mx.equilibrio.ui.theme.tabularAmountStyle

val EqAccountCardHeight = 184.dp

/** Familia de dominio del gradiente: verde para dinero propio, morado para crédito. */
enum class EqAccountFamily { GREEN, PURPLE }

/** (profundo, base, medio) de la familia de dominio. */
private fun accountPalette(family: EqAccountFamily, colors: EquilibrioColors): Triple<Color, Color, Color> =
    when (family) {
        EqAccountFamily.PURPLE -> Triple(colors.purpleDeep, colors.purple, colors.purpleMid)
        EqAccountFamily.GREEN -> Triple(colors.greenDeep, colors.green, colors.greenMid)
    }

fun eqAccountGradient(family: EqAccountFamily, colorSlot: Int, colors: EquilibrioColors): Brush {
    val (deep, base, mid) = accountPalette(family, colors)
    val (from, to) = when (((colorSlot % 3) + 3) % 3) {
        0 -> deep to mid
        1 -> base to deep
        else -> mid to base
    }
    return Brush.linearGradient(listOf(from, to))
}

/** Color sólido representativo de un slot, para el picker (el gradiente completo vive solo en [EqAccountCard]). */
fun eqAccountSlotColor(family: EqAccountFamily, colorSlot: Int, colors: EquilibrioColors): Color {
    val (deep, base, mid) = accountPalette(family, colors)
    return when (((colorSlot % 3) + 3) % 3) {
        0 -> base
        1 -> deep
        else -> mid
    }
}

/** Tarjeta de cuenta tipo billetera: gradiente por familia/colorSlot, dígitos enmascarados, disponible y vencimiento. */
@Composable
fun EqAccountCard(
    title: String,
    maskedDigits: String,
    availableCents: Long,
    family: EqAccountFamily,
    colorSlot: Int,
    modifier: Modifier = Modifier,
    dueDay: Int? = null,
    /** Disponible incluyendo cuotas SCHEDULED a futuro; null si no aplica (no es tarjeta de crédito). */
    projectedCents: Long? = null,
    /** Si se pasa, la tarjeta completa es tocable (ripple recortado a su forma, sin cortar la sombra). */
    onClick: (() -> Unit)? = null,
) {
    val colors = EquilibrioTheme.colors
    Column(
        modifier = modifier
            .eqShadow(Elevation.LEVEL_2, ShapeLarge)
            .background(eqAccountGradient(family, colorSlot, colors), ShapeLarge)
            .then(if (onClick != null) Modifier.clip(ShapeLarge).clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = EquilibrioTheme.typography.h3,
                    color = colors.onGradient,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = maskedDigits,
                    style = EquilibrioTheme.typography.label,
                    color = colors.onGradient.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
            // Sello de marca, como el de la red en una tarjeta bancaria.
            EqLogoMark(color = colors.onGradient.copy(alpha = 0.85f), width = 34.dp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "Disponible",
                    style = EquilibrioTheme.typography.caption,
                    color = colors.onGradient.copy(alpha = 0.7f),
                )
                Text(
                    text = formatCents(availableCents),
                    style = tabularAmountStyle(fontSize = 28.sp),
                    color = colors.onGradient,
                )
                if (projectedCents != null) {
                    Text(
                        text = "Proyectado: ${formatCents(projectedCents)}",
                        style = EquilibrioTheme.typography.caption,
                        color = colors.onGradient.copy(alpha = 0.7f),
                    )
                }
            }
            if (dueDay != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Vence",
                        style = EquilibrioTheme.typography.caption,
                        color = colors.onGradient.copy(alpha = 0.7f),
                    )
                    Text(
                        text = "Día $dueDay",
                        style = EquilibrioTheme.typography.label,
                        color = colors.onGradient,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqAccountCardPreview() {
    EquilibrioTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.base),
            verticalArrangement = Arrangement.spacedBy(Spacing.base),
        ) {
            EqAccountCard(
                title = "Cuenta débito",
                maskedDigits = "•••• 4821",
                availableCents = 1_254_300,
                family = EqAccountFamily.GREEN,
                colorSlot = 0,
                modifier = Modifier.width(320.dp).height(EqAccountCardHeight),
            )
            EqAccountCard(
                title = "Tarjeta oro",
                maskedDigits = "****",
                availableCents = 850_000,
                family = EqAccountFamily.PURPLE,
                colorSlot = 1,
                dueDay = 12,
                modifier = Modifier.width(320.dp).height(EqAccountCardHeight),
            )
        }
    }
}
