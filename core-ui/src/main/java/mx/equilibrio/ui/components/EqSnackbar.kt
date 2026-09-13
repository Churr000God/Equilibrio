package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.FeedbackTone
import mx.equilibrio.ui.theme.ShapeSmall
import mx.equilibrio.ui.theme.Spacing

/**
 * Contenido de `SnackbarHost`. Solo acepta `FeedbackTone` — nunca un color
 * de dominio, aunque el mensaje hable de un ingreso o un gasto (§6.5).
 */
@Composable
fun EqSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
    tone: FeedbackTone = FeedbackTone.SUCCESS,
) {
    val colors = EquilibrioTheme.colors
    val icon = if (tone == FeedbackTone.SUCCESS) Icons.Rounded.CheckCircle else Icons.Rounded.Info

    Snackbar(
        modifier = modifier.padding(Spacing.base),
        shape = ShapeSmall,
        containerColor = colors.ink,
        contentColor = colors.surface,
        action = data.visuals.actionLabel?.let { label ->
            {
                TextButton(onClick = { data.performAction() }) {
                    Text(label, color = colors.purpleMid)
                }
            }
        },
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.sm),
        ) {
            androidx.compose.material3.Icon(
                icon,
                contentDescription = null,
                tint = if (tone == FeedbackTone.SUCCESS) colors.green else colors.info,
            )
            Text(data.visuals.message)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqSnackbarPreview() {
    EquilibrioTheme {
        androidx.compose.material3.Snackbar(modifier = Modifier.padding(Spacing.base)) {
            Text("Cambios guardados.")
        }
    }
}
