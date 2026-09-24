package mx.equilibrio.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveTransactions
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    observeTransactions: ObserveTransactions,
    getCategories: GetCategories,
    observeAccounts: ObserveAccounts,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    /** Solo de esta sesión — no se persiste, como pidió el alcance de v1. */
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())

    val state: StateFlow<SearchUiState> = combine(
        _query,
        observeTransactions(),
        combine(getCategories(), observeAccounts(), ::Pair),
        _recentSearches,
    ) { query, transactions, (categories, accounts), recent ->
        val categoriesById = categories.associateBy { it.id }
        val accountsById = accounts.associateBy { it.id }

        val trimmed = query.trim()
        val results = if (trimmed.isBlank()) {
            emptyList()
        } else {
            transactions
                .map { tx ->
                    tx.toMovementUi(
                        categoryName = tx.categoryId?.let { categoriesById[it]?.name },
                        categoryIcon = tx.categoryId?.let { categoriesById[it]?.icon },
                        accountName = accountsById[tx.accountId]?.name,
                    )
                }
                .filter { movement ->
                    listOfNotNull(movement.note, movement.categoryName, movement.accountName)
                        .any { it.contains(trimmed, ignoreCase = true) }
                }
                .sortedByDescending { it.occurredAt }
        }

        SearchUiState(
            query = query,
            recentSearches = recent,
            results = results,
            totalCents = results.periodTotalCents(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState(),
    )

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> _query.value = event.text
            is SearchEvent.RecentSearchClicked -> _query.value = event.text
            SearchEvent.Submitted -> commitRecentSearch()
            is SearchEvent.RecentSearchRemoved -> _recentSearches.update { it - event.text }
        }
    }

    private fun commitRecentSearch() {
        val query = _query.value.trim()
        if (query.isBlank()) return
        _recentSearches.update { current ->
            (listOf(query) + current.filterNot { it.equals(query, ignoreCase = true) }).take(MAX_RECENT_SEARCHES)
        }
    }

    private companion object {
        const val MAX_RECENT_SEARCHES = 5
    }
}
