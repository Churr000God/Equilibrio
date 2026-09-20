package mx.equilibrio.feature.goals

sealed interface GoalsEvent {
    data class ContributeRequested(val goalId: String) : GoalsEvent
    data class ContributeAmountChanged(val raw: String) : GoalsEvent
    data class ContributeAccountChanged(val accountId: String) : GoalsEvent
    data object ContributeConfirmed : GoalsEvent
    data object ContributeDismissed : GoalsEvent

    data class DeleteRequested(val goal: GoalUi) : GoalsEvent
    data object DeleteConfirmed : GoalsEvent
    data object DeleteCancelled : GoalsEvent
}
