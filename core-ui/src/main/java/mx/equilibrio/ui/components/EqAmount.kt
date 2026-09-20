package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.color
import mx.equilibrio.ui.theme.tabularAmountStyle
import java.text.NumberFormat
import java.util.Locale

private val PesoFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

fun formatCents(amountCents: Long): String = PesoFormat.format(amountCents / 100.0)

/**
 * Todo monto pasa por aquí: cifras tabulares siempre, nunca un `Text` crudo
 * (§06). Nunca acepta `FeedbackTone` ni un `Color` — solo `DomainTone`.
 */
@Composable
fun EqAmount(
    amountCents: Long,
    tone: DomainTone,
    modifier: Modifier = Modifier,
    showSign: Boolean = false,
    isNegative: Boolean = false,
    fontSize: TextUnit = 15.sp,
    weight: FontWeight = FontWeight.Bold,
) {
    val prefix = when {
        !showSign -> ""
        isNegative -> "− "
        else -> "+"
    }
    Text(
        text = prefix + formatCents(amountCents),
        style = tabularAmountStyle(fontSize = fontSize, weight = weight),
        color = tone.color(EquilibrioTheme.colors),
        textAlign = TextAlign.End,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun EqAmountPreview() {
    EquilibrioTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(Spacing.base)) {
            EqAmount(544000, DomainTone.INCOME_FIXED, fontSize = 40.sp, showSign = true)
            EqAmount(8500, DomainTone.RECREATIONAL, showSign = true, isNegative = true)
            EqAmount(120000, DomainTone.ESSENTIAL, showSign = true, isNegative = true)
            EqAmount(0, DomainTone.NEUTRAL)
        }
    }
}
