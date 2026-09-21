package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.tabularAmountStyle
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Tile de estadística con variación contra el periodo anterior. La flecha
 * codifica dato (verde sube, morado baja — nunca rojo/ámbar): invariante I2.
 * `deltaPct` null = sin base de comparación.
 */
@Composable
fun EqStatTile(
    label: String,
    amountCents: Long,
    deltaPct: Float?,
    modifier: Modifier = Modifier,
    tone: DomainTone = DomainTone.NEUTRAL,
) {
    val colors = EquilibrioTheme.colors
    EqCard(modifier = modifier, elevation = Elevation.LEVEL_1, padding = Spacing.md) {
        Text(text = label, style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
        Text(
            text = formatCents(amountCents),
            style = tabularAmountStyle(fontSize = 18.sp),
            color = colors.ink,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(top = Spacing.xs),
        ) {
            if (deltaPct == null) {
                Text("Sin mes anterior", style = EquilibrioTheme.typography.caption, color = colors.inkFaint)
            } else {
                val up = deltaPct >= 0f
                val color = if (up) colors.green else colors.purple
                Icon(
                    imageVector = if (up) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = (if (up) "+" else "−") + "${(abs(deltaPct) * 100).roundToInt()} %",
                    style = tabularAmountStyle(fontSize = 12.sp),
                    color = color,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqStatTilePreview() {
    EquilibrioTheme {
        Row(modifier = Modifier.padding(Spacing.base), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            EqStatTile("Ingresos", 18_400_00, 0.04f, Modifier.weight(1f))
            EqStatTile("Gastos", 12_960_00, -0.06f, Modifier.weight(1f))
            EqStatTile("Ahorro", 5_440_00, null, Modifier.weight(1f))
        }
    }
}
