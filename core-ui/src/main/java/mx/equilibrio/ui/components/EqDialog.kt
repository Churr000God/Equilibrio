package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
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

/** Diálogo genérico con contenido custom (p. ej. un formulario corto), mismo shape/color que el resto del sistema. */
@Composable
fun EqFormDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Cancelar",
    confirmLoading: Boolean = false,
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = EquilibrioTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title, style = EquilibrioTheme.typography.h2, color = colors.ink) },
        text = content,
        confirmButton = {
            EqButton(
                text = confirmLabel,
                onClick = onConfirm,
                loading = confirmLoading,
                enabled = confirmEnabled,
                fullWidth = false,
            )
        },
        dismissButton = {
            EqButton(
                text = dismissLabel,
                onClick = onDismiss,
                variant = EqButtonVariant.SECONDARY,
                fullWidth = false,
            )
        },
        containerColor = colors.surface,
        shape = ShapeMedium,
    )
}

/** Diálogo informativo, no destructivo (p. ej. límite del plan freemium en RF10). */
@Composable
fun EqInfoDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    /** Reemplaza el ícono de info (p. ej. [EqPremiumBadge] en los avisos del plan). */
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = EquilibrioTheme.colors
    AlertDialog(
        onDismissRequest = onConfirm,
        icon = icon ?: {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = colors.info,
                modifier = Modifier
                    .background(colors.infoSoft, ShapeMedium)
                    .padding(Spacing.sm)
                    .size(24.dp),
            )
        },
        title = { Text(title, style = EquilibrioTheme.typography.h2, color = colors.ink) },
        text = { Text(body, style = EquilibrioTheme.typography.body, color = colors.inkMuted) },
        confirmButton = {
            EqButton(text = confirmLabel, onClick = onConfirm, fullWidth = false)
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
