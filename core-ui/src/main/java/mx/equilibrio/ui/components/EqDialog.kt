package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing

/**
 * Diálogo bloqueante para una acción destructiva e irreversible (§6.5).
 * "Cancelar" siempre pesa visualmente más que la acción destructiva.
 */
@Composable
fun EqDestructiveDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = EquilibrioTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier
                    .background(colors.errorSoft, ShapeMedium)
                    .padding(Spacing.sm)
                    .size(24.dp),
            )
        },
        title = { Text(title, style = EquilibrioTheme.typography.h2, color = colors.ink) },
        text = { Text(body, style = EquilibrioTheme.typography.body, color = colors.inkMuted) },
        confirmButton = {
            EqButton(
                text = confirmLabel,
                onClick = onConfirm,
                variant = EqButtonVariant.DESTRUCTIVE_LOW_EMPHASIS,
                fullWidth = false,
            )
        },
        dismissButton = {
            EqButton(
                text = "Cancelar",
                onClick = onDismiss,
                variant = EqButtonVariant.PRIMARY,
                fullWidth = false,
            )
        },
        containerColor = colors.surface,
        shape = ShapeMedium,
    )
}

@Preview(showBackground = true)
@Composable
private fun EqDestructiveDialogPreview() {
    EquilibrioTheme {
        EqDestructiveDialog(
            title = "¿Eliminar este movimiento?",
            body = "Gasto recreativo de $500.00 del 12 de septiembre.",
            confirmLabel = "Eliminar",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
