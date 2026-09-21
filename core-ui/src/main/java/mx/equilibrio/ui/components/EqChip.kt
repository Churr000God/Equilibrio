package mx.equilibrio.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing

/**
 * Chip de categoría (§10 tabla de estados): blanco con borde en reposo,
 * relleno sólido morado con texto blanco al seleccionar.
 */
@Composable
fun EqChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = EquilibrioTheme.colors
    val background = if (selected) colors.purple else colors.surface
    val foreground = if (selected) Color.White else colors.ink
    Row(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clickable(onClick = onClick)
            .background(background, ShapePill)
            .border(BorderStroke(1.dp, if (selected) colors.purple else colors.border), ShapePill)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(16.dp))
        }
        Text(label, style = EquilibrioTheme.typography.label, color = foreground)
    }
}
