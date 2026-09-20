package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeSmall
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.deepColor
import mx.equilibrio.ui.theme.softColor
import mx.equilibrio.ui.theme.tabularAmountStyle
import kotlin.math.roundToInt

/** Celda de la matriz ingreso × gasto: relleno suave, texto en tono profundo (§10). */
@Composable
fun EqMatrixCell(
    amountCents: Long,
    share: Float,
    tone: DomainTone,
    modifier: Modifier = Modifier,
) {
    val colors = EquilibrioTheme.colors
    Column(
        modifier = modifier
            .background(tone.softColor(colors), ShapeSmall)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = formatCents(amountCents),
            style = tabularAmountStyle(fontSize = 15.sp),
            color = tone.deepColor(colors),
        )
        Text(
            text = "${(share * 100).roundToInt()} %",
            style = tabularAmountStyle(fontSize = 11.sp, weight = androidx.compose.ui.text.font.FontWeight.Medium),
            color = tone.deepColor(colors),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EqMatrixCellPreview() {
    EquilibrioTheme {
        EqMatrixCell(7_100_00, 0.55f, DomainTone.ESSENTIAL, Modifier.padding(Spacing.base))
    }
}
