package mx.equilibrio.feature.goals

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqDestructiveDialog
import mx.equilibrio.ui.components.EqInlineValidation
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import kotlin.time.Instant

@Composable
fun GoalEditorScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun handleBack() {
        if (state.hasInput && !state.isEditing) showDiscardConfirm = true else onCancel()
    }

    if (showDiscardConfirm) {
        EqDestructiveDialog(
            title = "¿Descartar esta meta?",
            body = "Perderás lo que llevas capturado.",
            confirmLabel = "Descartar",
            onConfirm = onCancel,
            onDismiss = { showDiscardConfirm = false },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(
            title = if (state.isEditing) "Editar meta" else "Nueva meta",
            onBack = ::handleBack,
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
            EqTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(GoalEditorEvent.NameChanged(it)) },
                label = "Nombre",
                isError = state.nameError != null,
                helperOrError = state.nameError ?: "Algo concreto: 'Viaje a la playa', 'Laptop nueva'.",
            )

            EqTextField(
                value = state.targetInput,
                onValueChange = { viewModel.onEvent(GoalEditorEvent.TargetChanged(it)) },
                label = "Monto objetivo",
                keyboardType = KeyboardType.Decimal,
                isError = state.targetError != null,
                helperOrError = state.targetError,
            )

            DeadlineSection(
                deadline = state.deadline,
                error = state.deadlineError,
                onDateSelected = { viewModel.onEvent(GoalEditorEvent.DeadlineChanged(it)) },
            )

            EqButton(
                text = if (state.isEditing) "Guardar cambios" else "Crear meta",
                onClick = { viewModel.onEvent(GoalEditorEvent.SaveClicked) },
                enabled = state.canSave,
                loading = state.isSaving,
                modifier = Modifier.padding(vertical = Spacing.base),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlineSection(
    deadline: LocalDate?,
    error: String?,
    onDateSelected: (LocalDate?) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TextButton(onClick = { showPicker = true }) {
                Text(
                    text = deadline?.let { "Plazo: ${it.shortLabel()}" } ?: "Plazo (opcional)",
                    style = EquilibrioTheme.typography.bodyStrong,
                )
            }
            if (deadline != null) {
                TextButton(onClick = { onDateSelected(null) }) {
                    Text("Quitar", style = EquilibrioTheme.typography.bodyStrong, color = EquilibrioTheme.colors.inkMuted)
                }
            }
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
