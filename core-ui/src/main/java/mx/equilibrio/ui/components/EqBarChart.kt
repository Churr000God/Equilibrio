package mx.equilibrio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

/** Un par de barras por periodo: ingreso (verde) y gasto (morado). */
data class BarPair(val label: String, val incomeCents: Long, val expenseCents: Long)

/**
 * Gráfica de barras agrupadas dibujada en Canvas. Solo colores de dominio
 * (I2): el periodo destacado en tono base, el resto en tono suave para que
 * el mes actual lea primero.
 */
@Composable
fun EqBarChart(
    bars: List<BarPair>,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    highlightIndex: Int = bars.lastIndex,
) {
    val colors = EquilibrioTheme.colors
    val max = bars.maxOfOrNull { maxOf(it.incomeCents, it.expenseCents) }?.coerceAtLeast(1) ?: 1

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            if (bars.isEmpty()) return@Canvas
            val groupWidth = size.width / bars.size
            val barWidth = (groupWidth * 0.28f).coerceAtMost(14.dp.toPx())
            val gap = 3.dp.toPx()
            val radius = CornerRadius(barWidth / 2, barWidth / 2)
            val minBar = 3.dp.toPx()

            bars.forEachIndexed { index, bar ->
                val highlighted = index == highlightIndex
                val centerX = groupWidth * index + groupWidth / 2
                val incomeH = (size.height * bar.incomeCents / max).coerceAtLeast(if (bar.incomeCents > 0) minBar else 0f)
                val expenseH = (size.height * bar.expenseCents / max).coerceAtLeast(if (bar.expenseCents > 0) minBar else 0f)

                drawRoundRect(
                    color = if (highlighted) colors.green else colors.greenSoft,
                    topLeft = Offset(centerX - gap / 2 - barWidth, size.height - incomeH),
                    size = Size(barWidth, incomeH),
                    cornerRadius = radius,
                )
                drawRoundRect(
                    color = if (highlighted) colors.purple else colors.purpleSoft,
                    topLeft = Offset(centerX + gap / 2, size.height - expenseH),
                    size = Size(barWidth, expenseH),
                    cornerRadius = radius,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
            bars.forEachIndexed { index, bar ->
                Text(
                    text = bar.label,
                    style = EquilibrioTheme.typography.caption,
                    color = if (index == highlightIndex) colors.ink else colors.inkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun EqBarChartLegend(modifier: Modifier = Modifier) {
    val colors = EquilibrioTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        LegendDot(color = colors.green, label = "Ingreso")
        LegendDot(color = colors.purple, label = "Gasto")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(label, style = EquilibrioTheme.typography.caption, color = EquilibrioTheme.colors.inkMuted)
    }
}

@Preview(showBackground = true)
@Composable
private fun EqBarChartPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            EqBarChartLegend()
            EqBarChart(
                bars = listOf(
                    BarPair("Mar", 12_000_00, 9_000_00),
                    BarPair("Abr", 14_000_00, 11_000_00),
                    BarPair("May", 11_000_00, 10_500_00),
                    BarPair("Jun", 15_000_00, 9_800_00),
                    BarPair("Jul", 13_500_00, 12_000_00),
                    BarPair("Ago", 18_400_00, 12_960_00),
                ),
            )
        }
    }
}
