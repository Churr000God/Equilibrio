package mx.equilibrio.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqDomainToggleOption
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import kotlin.time.Instant

private val SCOPE_OPTIONS = listOf(
    "Semana" to DateScope.WEEK,
    "Mes" to DateScope.MONTH,
    "Año" to DateScope.YEAR,
    "Rango" to DateScope.RANGE,
)

/**
 * Comparte el `HomeViewModel` de Transacciones (scoped a su back stack entry en el nav host) —
 * los filtros se aplican en vivo, sin un mecanismo de "resultado de navegación": al volver, la
 * lista ya está filtrada porque es el mismo estado.
 */
@Composable
fun FiltersScreen(
    onApply: () -> Unit,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(
            title = "Filtros",
            onBack = onClosed,
            trailing = {
                TextButton(onClick = { viewModel.onEvent(HomeEvent.FiltersCleared) }) {
                    Text("Limpiar", style = EquilibrioTheme.typography.label, color = EquilibrioTheme.colors.ink)
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.base)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            DateScopeSection(
                scope = state.dateScope,
                rangeStart = state.rangeStart,
                rangeEnd = state.rangeEnd,
                onScopeChanged = { viewModel.onEvent(HomeEvent.DateScopeChanged(it)) },
                onRangeStartChanged = { viewModel.onEvent(HomeEvent.RangeStartChanged(it)) },
                onRangeEndChanged = { viewModel.onEvent(HomeEvent.RangeEndChanged(it)) },
            )

            FilterChipSection(
                title = "Tipo",
                options = listOf("Gastos" to TransactionKind.EXPENSE, "Ingresos" to TransactionKind.INCOME),
                selected = state.filterTypes,
                onToggle = { viewModel.onEvent(HomeEvent.TypeFilterToggled(it)) },
            )

            FilterChipSection(
                title = "Cuenta",
                options = state.accounts.map { it.name to it.id },
                selected = state.filterAccountIds,
                onToggle = { viewModel.onEvent(HomeEvent.AccountFilterToggled(it)) },
            )

            FilterChipSection(
                title = "Categoría",
                options = state.categories.sortedBy { it.name }.map { it.name to it.id },
                selected = state.filterCategoryIds,
                onToggle = { viewModel.onEvent(HomeEvent.CategoryFilterToggled(it)) },
            )

            FilterChipSection(
                title = "Estado",
                options = listOf("Confirmadas" to TransactionStatus.COMPLETED, "Programadas" to TransactionStatus.SCHEDULED),
                selected = state.filterStatuses,
                onToggle = { viewModel.onEvent(HomeEvent.StatusFilterToggled(it)) },
            )

            EqButton(
                text = "Aplicar filtros · ${state.transactions.size} resultados",
                onClick = { onApply() },
                modifier = Modifier.padding(vertical = Spacing.base),
            )
        }
    }
}

@Composable
private fun DateScopeSection(
    scope: DateScope,
    rangeStart: kotlinx.datetime.LocalDate?,
    rangeEnd: kotlinx.datetime.LocalDate?,
    onScopeChanged: (DateScope) -> Unit,
    onRangeStartChanged: (kotlinx.datetime.LocalDate) -> Unit,
    onRangeEndChanged: (kotlinx.datetime.LocalDate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = "Periodo", style = EquilibrioTheme.typography.bodySmall, color = EquilibrioTheme.colors.inkMuted)
        EqSegmentedControl(
            options = SCOPE_OPTIONS.map { it.first },
            selectedIndex = SCOPE_OPTIONS.indexOfFirst { it.second == scope },
            onSelect = { index -> onScopeChanged(SCOPE_OPTIONS[index].second) },
        )
        if (scope == DateScope.RANGE) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                DatePickerField(
                    label = "Desde",
                    date = rangeStart,
                    onDateSelected = onRangeStartChanged,
                    modifier = Modifier.weight(1f),
                )
                DatePickerField(
                    label = "Hasta",
                    date = rangeEnd,
                    onDateSelected = onRangeEndChanged,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: kotlinx.datetime.LocalDate?,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    TextButton(onClick = { showPicker = true }, modifier = modifier) {
        Text(
            text = date?.longLabel() ?: label,
            style = EquilibrioTheme.typography.bodyStrong,
            color = EquilibrioTheme.colors.ink,
        )
    }
    if (showPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
                    }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun <T> FilterChipSection(
    title: String,
    options: List<Pair<String, T>>,
    selected: Set<T>,
    onToggle: (T) -> Unit,
) {
    if (options.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = title, style = EquilibrioTheme.typography.bodySmall, color = EquilibrioTheme.colors.inkMuted)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            options.forEach { (label, value) ->
                EqDomainToggleOption(
                    label = label,
                    tone = DomainTone.NEUTRAL,
                    selected = value in selected,
                    onClick = { onToggle(value) },
                )
            }
        }
    }
}
