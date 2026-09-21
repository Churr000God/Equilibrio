package mx.equilibrio.feature.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Goal
import mx.equilibrio.domain.model.GoalStatus
import mx.equilibrio.domain.usecase.GetGoal
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.SaveGoal
import mx.equilibrio.ui.components.formatAmountInput
import mx.equilibrio.ui.components.sanitizeAmountInput
import mx.equilibrio.ui.components.toCentsOrZero
import java.util.UUID
import javax.inject.Inject

data class GoalEditorUiState(
    val name: String = "",
    val targetInput: String = "",
    val deadline: LocalDate? = null,
    val nameError: String? = null,
    val targetError: String? = null,
    val deadlineError: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
) {
    val targetCents: Long get() = targetInput.toCentsOrZero()
    val canSave: Boolean get() = name.isNotBlank() && targetCents > 0 && deadlineError == null && !isSaving
    val hasInput: Boolean get() = name.isNotBlank() || targetInput.isNotBlank() || deadline != null
}

sealed interface GoalEditorEvent {
    data class NameChanged(val text: String) : GoalEditorEvent
    data class TargetChanged(val raw: String) : GoalEditorEvent
    data class DeadlineChanged(val date: LocalDate?) : GoalEditorEvent
    data object SaveClicked : GoalEditorEvent
}

@HiltViewModel
class GoalEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAccounts: ObserveAccounts,
    private val getGoal: GetGoal,
    private val saveGoal: SaveGoal,
) : ViewModel() {

    private val editingId: String? = savedStateHandle["goalId"]

    private val _state = MutableStateFlow(GoalEditorUiState())
    val state: StateFlow<GoalEditorUiState> = _state.asStateFlow()

    private var existing: Goal? = null
    private var userId: String? = null

    init {
        viewModelScope.launch {
            // Sin login todavía: el userId viene de la cuenta sembrada.
            userId = observeAccounts().first().firstOrNull()?.userId
            val goal = editingId?.let { getGoal(it) } ?: return@launch
            existing = goal
            userId = goal.userId
            _state.update {
                it.copy(
                    name = goal.name,
                    targetInput = formatAmountInput(goal.targetCents),
                    deadline = goal.deadline,
                    isEditing = true,
                )
            }
        }
    }

    fun onEvent(event: GoalEditorEvent) {
        when (event) {
            is GoalEditorEvent.NameChanged -> _state.update { it.copy(name = event.text, nameError = null) }
            is GoalEditorEvent.TargetChanged -> _state.update {
                it.copy(targetInput = sanitizeAmountInput(event.raw), targetError = null)
            }
            is GoalEditorEvent.DeadlineChanged -> _state.update {
                it.copy(
                    deadline = event.date,
                    deadlineError = if (event.date != null && event.date <= today()) "Elige una fecha posterior a hoy." else null,
                )
            }
            GoalEditorEvent.SaveClicked -> save()
        }
    }

    private fun save() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(nameError = "Ponle nombre a tu meta.") }
            return
        }
        if (current.targetCents <= 0) {
            _state.update { it.copy(targetError = "Agrega un monto objetivo.") }
            return
        }
        if (!current.canSave) return
        val ownerId = userId ?: return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val base = existing
            saveGoal(
                Goal(
                    id = base?.id ?: UUID.randomUUID().toString(),
                    userId = ownerId,
                    name = current.name.trim(),
                    targetCents = current.targetCents,
                    savedCents = base?.savedCents ?: 0,
                    deadline = current.deadline,
                    status = base?.status ?: GoalStatus.ACTIVE,
                    createdAt = base?.createdAt ?: today(),
                ),
            )
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
