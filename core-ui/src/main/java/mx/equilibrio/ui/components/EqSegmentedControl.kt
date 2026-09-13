package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget

/** Control segmentado genérico de dos o más opciones (p. ej. Gasto / Ingreso). */
@Composable
fun EqSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.minSize)
            .background(colors.neutralBadge, ShapePill)
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
                    .background(if (selected) colors.surface else Color.Transparent, ShapePill)
                    .padding(vertical = Spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = EquilibrioTheme.typography.label,
                    color = if (selected) colors.ink else colors.inkMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqSegmentedControlPreview() {
    EquilibrioTheme {
        EqSegmentedControl(
            options = listOf("Gasto", "Ingreso"),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier.padding(Spacing.base),
        )
    }
}
