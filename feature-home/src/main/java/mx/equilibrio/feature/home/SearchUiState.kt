package mx.equilibrio.feature.home

import mx.equilibrio.domain.model.TransactionStatus

data class SearchUiState(
    val query: String = "",
    val recentSearches: List<String> = emptyList(),
    val results: List<TransactionUi> = emptyList(),
    val totalCents: Long = 0,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val resultCount: Int get() = results.size
}

internal fun List<TransactionUi>.periodTotalCents(): Long =
    filter { it.status == TransactionStatus.COMPLETED && !it.isSavings }
        .sumOf { if (it.isExpense) -it.amountCents else it.amountCents }

sealed interface SearchEvent {
    data class QueryChanged(val text: String) : SearchEvent
    data object Submitted : SearchEvent
    data class RecentSearchClicked(val text: String) : SearchEvent
    data class RecentSearchRemoved(val text: String) : SearchEvent
}
