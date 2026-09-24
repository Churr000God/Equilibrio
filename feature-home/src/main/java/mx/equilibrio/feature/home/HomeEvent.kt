package mx.equilibrio.feature.home

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus

sealed interface HomeEvent {
    data object PreviousMonth : HomeEvent
    data object NextMonth : HomeEvent
    data class AlertDismissed(val alertId: String) : HomeEvent

    data class DateScopeChanged(val scope: DateScope) : HomeEvent
    data class RangeStartChanged(val date: LocalDate) : HomeEvent
    data class RangeEndChanged(val date: LocalDate) : HomeEvent
    data class TypeFilterToggled(val kind: TransactionKind) : HomeEvent
    data class AccountFilterToggled(val accountId: String) : HomeEvent
    data class CategoryFilterToggled(val categoryId: String) : HomeEvent
    data class StatusFilterToggled(val status: TransactionStatus) : HomeEvent
    data object FiltersCleared : HomeEvent
}
