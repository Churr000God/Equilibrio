package mx.equilibrio.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.Goal
import mx.equilibrio.domain.usecase.ContributeToGoal
import mx.equilibrio.domain.usecase.DeleteGoal
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveGoals
import mx.equilibrio.domain.usecase.ObserveSavingStreak
import mx.equilibrio.ui.components.sanitizeAmountInput
import javax.inject.Inject
import kotlin.time.Clock

internal fun today(): LocalDate = Clock.System.todayIn(TimeZone.UTC)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    observeGoals: ObserveGoals,
    observeSavingStreak: ObserveSavingStreak,
    private val observeAccounts: ObserveAccounts,
    private val contributeToGoal: ContributeToGoal,
    private val deleteGoal: DeleteGoal,
) : ViewModel() {

    private val contribute = MutableStateFlow<ContributeUi?>(null)
    private val pendingDeletion = MutableStateFlow<GoalUi?>(null)

    val state: StateFlow<GoalsUiState> = combine(
        observeGoals(),
        observeSavingStreak(today()),
        contribute,
        pendingDeletion,
    ) { goals, streak, sheet, pending ->
        val (done, active) = goals.partition { it.isCompleted }
        // Destacada: la activa con mayor avance. Empate → la más reciente (ya vienen ordenadas así).
        val featured = active.maxByOrNull { it.progress }
        GoalsUiState(
            isLoading = false,
            featured = featured?.toUi(),
            others = active.filter { it.id != featured?.id }.map(Goal::toUi),
            completed = done.map(Goal::toUi),
            streakWeeks = streak,
            totalSavedCents = goals.sumOf { it.savedCents },
            contribute = sheet,
            pendingDeletion = pending,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState(isLoading = true),
    )

    fun onEvent(event: GoalsEvent) {
        when (event) {
            is GoalsEvent.ContributeRequested -> openContribute(event.goalId)
            is GoalsEvent.ContributeAmountChanged -> contribute.update {
                it?.copy(amountInput = sanitizeAmountInput(event.raw), amountError = null)
            }
            is GoalsEvent.ContributeAccountChanged -> contribute.update { it?.copy(accountId = event.accountId) }
            GoalsEvent.ContributeConfirmed -> confirmContribute()
            GoalsEvent.ContributeDismissed -> contribute.update { null }

            is GoalsEvent.DeleteRequested -> pendingDeletion.update { event.goal }
            GoalsEvent.DeleteCancelled -> pendingDeletion.update { null }
            GoalsEvent.DeleteConfirmed -> {
                val target = pendingDeletion.value ?: return
                pendingDeletion.update { null }
                viewModelScope.launch { deleteGoal(target.id) }
            }
        }
    }

    private fun openContribute(goalId: String) {
        val goal = (state.value.featured?.takeIf { it.id == goalId }
            ?: state.value.others.firstOrNull { it.id == goalId }) ?: return
        viewModelScope.launch {
            val accounts = observeAccounts().first()
            contribute.update {
                ContributeUi(
                    goalId = goal.id,
                    goalName = goal.name,
                    accounts = accounts,
                    accountId = accounts.firstOrNull()?.id,
                )
            }
        }
    }

    private fun confirmContribute() {
        val sheet = contribute.value ?: return
        if (sheet.amountCents <= 0) {
            contribute.update { it?.copy(amountError = "Agrega un monto para abonar.") }
            return
        }
        val accountId = sheet.accountId ?: return
        contribute.update { it?.copy(isSaving = true) }
        viewModelScope.launch {
            contributeToGoal(sheet.goalId, accountId, sheet.amountCents, today())
            contribute.update { null }
        }
    }
}
