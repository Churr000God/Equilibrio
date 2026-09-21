package mx.equilibrio.feature.reports

import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.model.report.Report

data class ReportsUiState(
    val month: YearMonth,
    val isLoading: Boolean = true,
    val report: Report? = null,
    val canGoForward: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && (report?.isEmpty ?: true)
}

sealed interface ReportsEvent {
    data object PreviousMonth : ReportsEvent
    data object NextMonth : ReportsEvent
}
