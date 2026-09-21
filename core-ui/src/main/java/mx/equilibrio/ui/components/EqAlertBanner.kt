package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.FeedbackTone
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing

/**
 * Banner de alerta del sistema (RF09) — nunca dentro del AppBar, siempre debajo.
 * Solo acepta `FeedbackTone`, igual que `EqSnackbar` (§6.5, invariante I2).
 */
@Composable
fun EqAlertBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tone: FeedbackTone = FeedbackTone.WARNING,
) {
    val colors = EquilibrioTheme.colors
    val (background, foreground, icon) = when (tone) {
        FeedbackTone.SUCCESS -> Triple(colors.greenSoft, colors.greenDeep, Icons.Rounded.CheckCircle)
        FeedbackTone.ERROR -> Triple(colors.errorSoft, colors.errorDeep, Icons.Rounded.Error)
        FeedbackTone.INFO -> Triple(colors.infoSoft, colors.info, Icons.Rounded.Info)
        FeedbackTone.WARNING -> Triple(colors.warningSoft, colors.warning, Icons.Rounded.Warning)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background, ShapeMedium)
            .then(
                if (EquilibrioTheme.accessibilityMode) {
                    Modifier.border(2.dp, foreground, ShapeMedium)
                } else {
                    Modifier
                },
            )
            .padding(start = Spacing.base, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(icon, contentDescription = null, tint = foreground)
        Text(
            text = message,
            style = EquilibrioTheme.typography.bodySmall,
            color = foreground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = "Descartar alerta", tint = foreground)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqAlertBannerPreview() {
    EquilibrioTheme {
        EqAlertBanner(
            message = "Tu gasto recreativo se desvió de tu equilibrio habitual.",
            onDismiss = {},
        )
    }
}
