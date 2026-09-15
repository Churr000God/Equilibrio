package mx.equilibrio.feature.home

sealed interface HomeEvent {
    data class DeleteRequested(val transaction: TransactionUi) : HomeEvent
    data object DeleteConfirmed : HomeEvent
    data object DeleteCancelled : HomeEvent
    data class AlertDismissed(val alertId: String) : HomeEvent
}
