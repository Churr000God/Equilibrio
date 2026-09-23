package mx.equilibrio.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqButtonVariant
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing

/**
 * Confirmación de borrado con dos destinos para los movimientos existentes
 * (mock designs/categorias.png, frame 5). Reutilizado por la pantalla de
 * edición y la de detalle.
 */
@Composable
fun CategoryDeleteDialog(
    categoryName: String,
    movementsCount: Int,
    onReassign: () -> Unit,
    onDeleteAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = EquilibrioTheme.colors
    var reassign by remember { mutableStateOf(true) }

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
        title = { Text("¿Eliminar \"$categoryName\"?", style = EquilibrioTheme.typography.h2, color = colors.ink) },
        text = {
            Column {
                Text(
                    text = "Esta categoría tiene $movementsCount movimientos registrados. Elige qué hacer con ellos antes de continuar.",
                    style = EquilibrioTheme.typography.body,
                    color = colors.inkMuted,
                )
                Column(modifier = Modifier.padding(top = Spacing.md)) {
                    DeleteOption(
                        title = "Moverlos a \"Otros\"",
                        subtitle = "Los movimientos se conservan",
                        selected = reassign,
                        onClick = { reassign = true },
                    )
                    DeleteOption(
                        title = "Eliminarlos también",
                        subtitle = "Se borran $movementsCount movimientos",
                        selected = !reassign,
                        onClick = { reassign = false },
                    )
                }
            }
        },
        confirmButton = {
            EqButton(
                text = "Eliminar",
                variant = EqButtonVariant.DESTRUCTIVE,
                fullWidth = false,
                onClick = { if (reassign) onReassign() else onDeleteAll() },
            )
        },
        dismissButton = {
            EqButton(text = "Cancelar", variant = EqButtonVariant.SECONDARY, fullWidth = false, onClick = onDismiss)
        },
    )
}

@Composable
private fun DeleteOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = Spacing.xs),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = colors.green, unselectedColor = colors.inkFaint),
        )
        Column(modifier = Modifier.padding(start = Spacing.xs)) {
            Text(text = title, style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
            Text(text = subtitle, style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
        }
    }
}
