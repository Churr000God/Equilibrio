package mx.equilibrio.feature.home

sealed interface HomeEvent {
    data object PreviousMonth : HomeEvent
    data object NextMonth : HomeEvent
    data class AlertDismissed(val alertId: String) : HomeEvent
}
