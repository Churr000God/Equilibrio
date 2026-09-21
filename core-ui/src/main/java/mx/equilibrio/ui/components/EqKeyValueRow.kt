package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

/** Fila etiqueta+valor de una sola línea (p. ej. un resumen dentro de [EqCard]). */
@Composable
fun EqKeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = EquilibrioTheme.colors.ink,
) {
    Column(modifier = modifier) {
        Text(text = label, style = EquilibrioTheme.typography.bodySmall, color = EquilibrioTheme.colors.inkMuted)
        Text(text = value, style = EquilibrioTheme.typography.bodyStrong, color = valueColor)
    }
}

/** Varias [EqKeyValueRow] distribuidas en una fila (p. ej. gastado/pagado/saldo de un periodo). */
@Composable
fun EqKeyValueRowGroup(items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        items.forEach { (label, value) -> EqKeyValueRow(label = label, value = value) }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqKeyValueRowGroupPreview() {
    EquilibrioTheme {
        EqKeyValueRowGroup(
            modifier = Modifier.fillMaxWidth(),
            items = listOf(
                "Gastado" to "$1,240.00",
                "Pagado" to "$500.00",
                "Saldo" to "$740.00",
            ),
        )
    }
}
