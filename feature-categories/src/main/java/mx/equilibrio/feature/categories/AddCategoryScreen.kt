package mx.equilibrio.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.ui.components.CategoryIcons
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqColorSlotPicker
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

private val TYPE_OPTIONS = listOf("Gasto" to CategoryType.EXPENSE, "Ingreso" to CategoryType.INCOME)

@Composable
fun AddCategoryScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddCategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // onSaved cierra la pantalla tanto si se guardó como si se borró: ambos flujos terminan igual, no hay nada más que editar.
    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onSaved()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        CategoryDeleteDialog(
            categoryName = state.name,
            movementsCount = state.movementsCount,
            onReassign = {
                showDeleteConfirm = false
                viewModel.onEvent(AddCategoryEvent.DeleteConfirmed(reassignToOtros = true))
            },
            onDeleteAll = {
                showDeleteConfirm = false
                viewModel.onEvent(AddCategoryEvent.DeleteConfirmed(reassignToOtros = false))
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(
            title = if (state.isEditing) "Editar categoría" else "Nueva categoría",
            onBack = onCancel,
            // Las categorías de sistema (p. ej. "Otros") no se pueden borrar: son el destino de reasignación
            // cuando se elimina otra categoría, así que siempre debe haber una disponible.
            trailing = if (state.isEditing && !state.isSystem) {
                {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Eliminar categoría",
                            tint = EquilibrioTheme.colors.error,
                        )
                    }
                }
            } else null,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.base)
                .clipToBounds()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            EqSegmentedControl(
                options = TYPE_OPTIONS.map { it.first },
                selectedIndex = TYPE_OPTIONS.indexOfFirst { it.second == state.type },
                onSelect = { index -> viewModel.onEvent(AddCategoryEvent.TypeChanged(TYPE_OPTIONS[index].second)) },
            )

            EqTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(AddCategoryEvent.NameChanged(it)) },
                label = "Nombre de la categoría",
                isError = state.nameError != null,
                helperOrError = state.nameError,
            )

            CategoryIconPicker(
                type = state.type,
                colorSlot = state.colorSlot,
                selectedIcon = state.icon,
                onSelect = { viewModel.onEvent(AddCategoryEvent.IconChanged(it)) },
            )

            CategoryColorPicker(
                type = state.type,
                selectedSlot = state.colorSlot,
                onSelect = { viewModel.onEvent(AddCategoryEvent.ColorSlotChanged(it)) },
            )

            BudgetSection(
                enabled = state.budgetEnabled,
                amountInput = state.budgetInput,
                onToggle = { viewModel.onEvent(AddCategoryEvent.BudgetToggled(it)) },
                onAmountChanged = { viewModel.onEvent(AddCategoryEvent.BudgetAmountChanged(it)) },
            )

            EqButton(
                text = if (state.isEditing) "Guardar cambios" else "Guardar categoría",
                onClick = { viewModel.onEvent(AddCategoryEvent.SaveClicked) },
                enabled = state.canSave && !state.isDeleting,
                loading = state.isSaving,
                modifier = Modifier.padding(vertical = Spacing.base),
            )
        }
    }
}

@Composable
private fun CategoryIconPicker(
    type: CategoryType,
    colorSlot: Int,
    selectedIcon: String,
    onSelect: (String) -> Unit,
) {
    val colors = EquilibrioTheme.colors
    val tint = categoryColor(type, colorSlot, colors)

    Column {
        Text(text = "Ícono", style = EquilibrioTheme.typography.bodySmall, color = colors.inkMuted)
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.height(184.dp).padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(CategoryIcons.ALL, key = { it.first }) { (key, icon) ->
                val selected = key == selectedIcon
                Row(
                    modifier = Modifier
                        .size(40.dp)
                        .selectable(selected = selected, onClick = { onSelect(key) })
                        .background(if (selected) tint.copy(alpha = 0.15f) else colors.neutralBadge, CircleShape),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon, contentDescription = key, tint = if (selected) tint else colors.inkMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryColorPicker(
    type: CategoryType,
    selectedSlot: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = EquilibrioTheme.colors
    EqColorSlotPicker(
        colors = (0 until CategoryColorSlotCount).map { slot -> categoryColor(type, slot, colors) },
        selectedSlot = selectedSlot,
        onSelect = onSelect,
    )
}

@Composable
private fun BudgetSection(
    enabled: Boolean,
    amountInput: String,
    onToggle: (Boolean) -> Unit,
    onAmountChanged: (String) -> Unit,
) {
    val colors = EquilibrioTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Presupuesto mensual", style = EquilibrioTheme.typography.bodySmall, color = colors.inkMuted)
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = colors.surface, checkedTrackColor = colors.green),
            )
        }
        if (enabled) {
            EqTextField(
                value = amountInput,
                onValueChange = onAmountChanged,
                label = "Monto",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryColorPickerPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            CategoryColorPicker(type = CategoryType.EXPENSE, selectedSlot = 1, onSelect = {})
        }
    }
}
