package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.FeedbackTone
import mx.equilibrio.ui.theme.Spacing

/** Guía de un campo bajo su control: recuperable al instante (§6.5, Severidad Baja). */
@Composable
fun EqInlineValidation(
    message: String,
    modifier: Modifier = Modifier,
    tone: FeedbackTone = FeedbackTone.ERROR,
) {
    val colors = EquilibrioTheme.colors
    val color = when (tone) {
        FeedbackTone.ERROR -> colors.error
        FeedbackTone.WARNING -> colors.warning
        FeedbackTone.INFO -> colors.info
        FeedbackTone.SUCCESS -> colors.green
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text = message, style = EquilibrioTheme.typography.caption, color = color)
    }
}

@Preview(showBackground = true)
@Composable
private fun EqInlineValidationPreview() {
    EquilibrioTheme {
        EqInlineValidation(
            "Agrega un monto para guardar.",
            modifier = Modifier.padding(Spacing.base),
        )
    }
}
