package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing

/**
 * Selector genérico de un color entre una lista fija de swatches (p. ej. el
 * color de una categoría). El caller resuelve la paleta de dominio; este
 * componente no sabe nada de tipos de dominio, solo pinta `colors`.
 */
@Composable
fun EqColorSlotPicker(
    colors: List<Color>,
    selectedSlot: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Color",
    colorNames: List<String>? = null,
) {
    val theme = EquilibrioTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = label, style = EquilibrioTheme.typography.bodySmall, color = theme.inkMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            colors.forEachIndexed { slot, color ->
                val selected = slot == selectedSlot
                val description = colorNames?.getOrNull(slot) ?: "Color ${slot + 1}"
                Row(
                    modifier = Modifier
                        .size(40.dp)
                        .selectable(selected = selected, onClick = { onSelect(slot) }, role = Role.RadioButton)
                        .semantics { contentDescription = description }
                        .background(color, ShapePill)
                        .border(if (selected) 2.dp else 0.dp, theme.ink, ShapePill),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selected) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqColorSlotPickerPreview() {
    EquilibrioTheme {
        val colors = EquilibrioTheme.colors
        EqColorSlotPicker(
            colors = listOf(colors.purple, colors.purpleDeep, colors.purpleMid),
            selectedSlot = 1,
            onSelect = {},
            modifier = Modifier,
        )
    }
}
