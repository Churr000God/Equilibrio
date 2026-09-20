package mx.equilibrio.feature.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.CategoryType
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

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = { EqTopBar(title = "Nueva categoría", onBack = onCancel) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = Spacing.base)
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

            CategoryColorPicker(
                type = state.type,
                selectedSlot = state.colorSlot,
                onSelect = { viewModel.onEvent(AddCategoryEvent.ColorSlotChanged(it)) },
            )

            EqButton(
                text = "Guardar categoría",
                onClick = { viewModel.onEvent(AddCategoryEvent.SaveClicked) },
                enabled = state.canSave,
                loading = state.isSaving,
                modifier = Modifier.padding(vertical = Spacing.base),
            )
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

@Preview(showBackground = true)
@Composable
private fun CategoryColorPickerPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            CategoryColorPicker(type = CategoryType.EXPENSE, selectedSlot = 1, onSelect = {})
        }
    }
}
