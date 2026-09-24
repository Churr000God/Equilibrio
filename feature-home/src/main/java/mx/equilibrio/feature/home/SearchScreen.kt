package mx.equilibrio.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqDomainToggleOption
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqIllustration
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing

@Composable
fun SearchScreen(
    onTransactionClicked: (String) -> Unit,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = EquilibrioTheme.colors

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Spacing.base, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            EqTextField(
                value = state.query,
                onValueChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
                label = "Buscar movimientos",
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClosed) {
                Text("Cancelar", style = EquilibrioTheme.typography.label, color = colors.ink)
            }
        }

        if (!state.isSearching) {
            RecentSearchesSection(
                recent = state.recentSearches,
                onClick = { viewModel.onEvent(SearchEvent.RecentSearchClicked(it)) },
                onRemove = { viewModel.onEvent(SearchEvent.RecentSearchRemoved(it)) },
                modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.base),
            )
        } else if (state.results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(Spacing.base), contentAlignment = Alignment.Center) {
                EqEmptyState(
                    title = "Sin resultados",
                    body = "No encontramos movimientos que coincidan con \"${state.query}\".",
                    illustration = EqIllustration.NEUTRAL,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.base, vertical = Spacing.base),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                item(key = "summary") {
                    SearchSummaryCard(
                        count = state.resultCount,
                        query = state.query,
                        totalCents = state.totalCents,
                        modifier = Modifier.padding(bottom = Spacing.sm),
                    )
                }
                item(key = "results_title") {
                    Text(
                        text = "Resultados",
                        style = EquilibrioTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
                items(state.results, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = {
                            viewModel.onEvent(SearchEvent.Submitted)
                            if (!transaction.isSavings) onTransactionClicked(transaction.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSummaryCard(count: Int, query: String, totalCents: Long, modifier: Modifier = Modifier) {
    val colors = EquilibrioTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.neutralBadge, ShapeMedium)
            .padding(Spacing.base),
    ) {
        Text(
            text = "$count movimientos con \"$query\"",
            style = EquilibrioTheme.typography.bodyStrong,
            color = colors.ink,
        )
        Text(
            text = "Total del periodo ${formatCents(totalCents)}",
            style = EquilibrioTheme.typography.caption,
            color = colors.inkMuted,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun RecentSearchesSection(
    recent: List<String>,
    onClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recent.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = "Búsquedas recientes",
            style = EquilibrioTheme.typography.bodySmall,
            color = EquilibrioTheme.colors.inkMuted,
        )
        Row(
            modifier = Modifier.padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            recent.forEach { term ->
                EqDomainToggleOption(
                    label = term,
                    tone = DomainTone.NEUTRAL,
                    selected = false,
                    onClick = { onClick(term) },
                )
            }
        }
    }
}
