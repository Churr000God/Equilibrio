package mx.equilibrio.feature.entry

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqDestructiveDialog
import mx.equilibrio.ui.components.EqDomainToggleOption
import mx.equilibrio.ui.components.EqInlineValidation
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

@Composable
fun QuickEntryScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuickEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    var showDiscardConfirm by remember { mutableStateOf(false) }
    val hasUnsavedInput = state.amountInput.isNotBlank() || state.note.isNotBlank() || state.classification != null

    fun handleBack() {
        if (hasUnsavedInput) showDiscardConfirm = true else onCancel()
    }

    if (showDiscardConfirm) {
        EqDestructiveDialog(
            title = "¿Descartar este movimiento?",
            body = "Perderás lo que llevas capturado.",
            confirmLabel = "Descartar",
            onConfirm = onCancel,
            onDismiss = { showDiscardConfirm = false },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = {
            EqTopBar(
                title = if (state.isEditing) "Editar movimiento" else "Nuevo movimiento",
                onBack = ::handleBack,
            )
        },
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
                options = listOf("Gasto", "Ingreso"),
                selectedIndex = if (state.kind == TransactionKind.EXPENSE) 0 else 1,
                onSelect = { index ->
                    viewModel.onEvent(
                        QuickEntryEvent.KindChanged(
                            if (index == 0) TransactionKind.EXPENSE else TransactionKind.INCOME,
                        ),
                    )
                },
            )

            Column {
                EqTextField(
                    value = state.amountInput,
                    onValueChange = { viewModel.onEvent(QuickEntryEvent.AmountChanged(it)) },
                    label = "Monto",
                    keyboardType = KeyboardType.Decimal,
                    isError = state.amountError != null,
                    helperOrError = state.amountError,
                )
            }

            ClassificationSection(
                kind = state.kind,
                selected = state.classification,
                onSelect = { viewModel.onEvent(QuickEntryEvent.ClassificationChanged(it)) },
            )

            DateSection(
                dateLabel = state.occurredAt.toString(),
                error = state.dateError,
                onDateSelected = { viewModel.onEvent(QuickEntryEvent.DateChanged(it)) },
            )

            EqTextField(
                value = state.note,
                onValueChange = { viewModel.onEvent(QuickEntryEvent.NoteChanged(it)) },
                label = "Nota (opcional)",
            )

            EqButton(
                text = if (state.kind == TransactionKind.EXPENSE) "Guardar gasto" else "Guardar ingreso",
                onClick = { viewModel.onEvent(QuickEntryEvent.SaveClicked) },
                enabled = state.canSave,
                loading = state.isSaving,
                modifier = Modifier.padding(vertical = Spacing.base),
            )
        }
    }
}

@Composable
private fun ClassificationSection(
    kind: TransactionKind,
    selected: Classification?,
    onSelect: (Classification) -> Unit,
) {
    val options = if (kind == TransactionKind.EXPENSE) {
        listOf(
            "Esencial" to Classification.ESSENTIAL,
            "Recreativo" to Classification.RECREATIONAL,
        )
    } else {
        listOf(
            "Fijo" to Classification.FIXED,
            "Variable" to Classification.VARIABLE,
        )
    }
    val tones = if (kind == TransactionKind.EXPENSE) {
        DomainTone.ESSENTIAL to DomainTone.RECREATIONAL
    } else {
        DomainTone.INCOME_FIXED to DomainTone.INCOME_VARIABLE
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = if (kind == TransactionKind.EXPENSE) {
                "¿Este gasto es indispensable o es para disfrutar?"
            } else {
                "¿Este ingreso llega siempre igual o cambia cada mes?"
            },
            style = EquilibrioTheme.typography.bodySmall,
            color = EquilibrioTheme.colors.inkMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            EqDomainToggleOption(
                label = options[0].first,
                tone = tones.first,
                selected = selected == options[0].second,
                onClick = { onSelect(options[0].second) },
                modifier = Modifier.weight(1f),
            )
            EqDomainToggleOption(
                label = options[1].first,
                tone = tones.second,
                selected = selected == options[1].second,
                onClick = { onSelect(options[1].second) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSection(
    dateLabel: String,
    error: String?,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        TextButton(onClick = { showPicker = true }) {
            Text("Fecha: $dateLabel", style = EquilibrioTheme.typography.bodyStrong)
        }
        if (error != null) {
            EqInlineValidation(error)
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(date)
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
