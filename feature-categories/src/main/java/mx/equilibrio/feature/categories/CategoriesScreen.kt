package mx.equilibrio.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqIllustration
import mx.equilibrio.ui.components.EqFab
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqSkeleton
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing

private val TAB_OPTIONS = listOf("Gasto" to CategoryType.EXPENSE, "Ingreso" to CategoryType.INCOME)

@Composable
fun CategoriesScreen(
    onAddCategoryClicked: () -> Unit,
    onCategoryClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(title = "Categorías", trailing = trailing)

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> EqSkeleton(rowHeight = 56.dp)

                state.isEmpty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EqEmptyState(
                        title = "Aún no tienes categorías",
                        body = "Agrega categorías para organizar tus ingresos y gastos.",
                        illustration = EqIllustration.NEUTRAL,
                    )
                }

                else -> {
                    val shown = if (selectedType == CategoryType.EXPENSE) state.expenseCategories else state.incomeCategories
                    val total = if (selectedType == CategoryType.EXPENSE) state.expenseTotalCents else state.incomeTotalCents
                    val totalLabel = if (selectedType == CategoryType.EXPENSE) "Total gastado" else "Total recibido"

                    Column(modifier = Modifier.fillMaxSize()) {
                        EqSegmentedControl(
                            options = TAB_OPTIONS.map { it.first },
                            selectedIndex = TAB_OPTIONS.indexOfFirst { it.second == selectedType },
                            onSelect = { index -> selectedType = TAB_OPTIONS[index].second },
                            modifier = Modifier.padding(horizontal = Spacing.base),
                        )

                        CategoriesHeader(
                            label = totalLabel,
                            totalCents = total,
                            categories = shown,
                            monthLabel = state.month.label(),
                            canGoForward = state.canGoForward,
                            onPreviousMonth = { viewModel.onEvent(CategoriesEvent.PreviousMonth) },
                            onNextMonth = { viewModel.onEvent(CategoriesEvent.NextMonth) },
                            modifier = Modifier.padding(Spacing.base),
                        )

                        // clipToBounds(): el stretch de overscroll no debe pintar fuera de sus bounds.
                        LazyColumn(modifier = Modifier.fillMaxSize().clipToBounds()) {
                            items(shown, key = { it.id }) { category ->
                                CategoryRow(category = category, onClick = { onCategoryClicked(category.id) })
                            }
                        }
                    }
                }
            }

            EqFab(onClick = onAddCategoryClicked, modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg))
        }
    }
}

/** Total del periodo + barra de proporción por categoría (colores de dominio, invariante I2). */
@Composable
private fun CategoriesHeader(
    label: String,
    totalCents: Long,
    categories: List<CategoryUi>,
    monthLabel: String,
    canGoForward: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EquilibrioTheme.colors
    EqCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = label, style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
                Text(
                    text = formatCents(totalCents),
                    style = EquilibrioTheme.typography.h1,
                    color = colors.ink,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
            MonthSelector(
                label = monthLabel,
                canGoForward = canGoForward,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.md)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            val visible = categories.filter { it.amountCents > 0 }
            if (visible.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().background(colors.border))
            } else {
                visible.forEach { category ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(category.share.coerceAtLeast(0.001f))
                            .background(categoryColor(category.type, category.colorSlot, colors)),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(label: String, canGoForward: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = Modifier.background(colors.neutralBadge, ShapePill),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mes anterior", tint = colors.ink)
        }
        Text(
            text = label,
            style = EquilibrioTheme.typography.label,
            color = colors.ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 116.dp),
        )
        IconButton(onClick = onNext, enabled = canGoForward, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "Mes siguiente",
                tint = if (canGoForward) colors.ink else colors.border,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenEmptyPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            EqEmptyState(
                title = "Aún no tienes categorías",
                body = "Agrega categorías para organizar tus ingresos y gastos.",
            )
        }
    }
}
