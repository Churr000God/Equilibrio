package mx.equilibrio.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget

/**
 * Una opción del toggle de clasificación (§11.5: "Toggle Esencial/Recreativo").
 * Nunca viene preseleccionada — invariante I3, el usuario debe tocar.
 */
@Composable
fun EqDomainToggleOption(
    label: String,
    tone: DomainTone,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EquilibrioTheme.colors
    val (fill, border, accent) = when (tone) {
        DomainTone.ESSENTIAL, DomainTone.INCOME_FIXED -> Triple(colors.greenSoft, colors.green, colors.greenDeep)
        DomainTone.RECREATIONAL, DomainTone.INCOME_VARIABLE -> Triple(colors.purpleSoft, colors.purple, colors.purpleDeep)
        DomainTone.NEUTRAL -> Triple(colors.neutralBadge, colors.border, colors.inkMuted)
    }

    Row(
        modifier = modifier
            .heightIn(min = TouchTarget.minSize)
            .clickable(onClick = onClick)
            .background(if (selected) fill else colors.surface, ShapeMedium)
            .border(BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) border else colors.border), ShapeMedium)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = accent, modifier = Modifier)
        }
        Text(
            text = label,
            style = EquilibrioTheme.typography.bodyStrong,
            color = if (selected) accent else colors.ink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EqDomainToggleOptionPreview() {
    EquilibrioTheme {
        Row(
            modifier = Modifier.padding(Spacing.base),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            EqDomainToggleOption("Esencial", DomainTone.ESSENTIAL, selected = true, onClick = {})
            EqDomainToggleOption("Recreativo", DomainTone.RECREATIONAL, selected = false, onClick = {})
        }
    }
}
