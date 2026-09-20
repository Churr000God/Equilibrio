package mx.equilibrio.feature.goals

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.Goal
import mx.equilibrio.ui.components.toCentsOrZero

data class GoalUi(
    val id: String,
    val name: String,
    val targetCents: Long,
    val savedCents: Long,
    val deadline: LocalDate?,
    val progress: Float,
    val isCompleted: Boolean,
)

fun Goal.toUi() = GoalUi(
    id = id,
    name = name,
    targetCents = targetCents,
    savedCents = savedCents,
    deadline = deadline,
    progress = progress,
    isCompleted = isCompleted,
)

/** Hoja de abono abierta sobre una meta. */
data class ContributeUi(
    val goalId: String,
    val goalName: String,
    val amountInput: String = "",
    val accountId: String? = null,
    val accounts: List<Account> = emptyList(),
    val amountError: String? = null,
    val isSaving: Boolean = false,
) {
    val amountCents: Long get() = amountInput.toCentsOrZero()
    val canConfirm: Boolean get() = amountCents > 0 && accountId != null && !isSaving
}

data class GoalsUiState(
    val isLoading: Boolean = true,
    val featured: GoalUi? = null,
    val others: List<GoalUi> = emptyList(),
    val completed: List<GoalUi> = emptyList(),
    val streakWeeks: Int = 0,
    val totalSavedCents: Long = 0,
    val contribute: ContributeUi? = null,
    val pendingDeletion: GoalUi? = null,
) {
    val isEmpty: Boolean get() = !isLoading && featured == null && others.isEmpty() && completed.isEmpty()
}
