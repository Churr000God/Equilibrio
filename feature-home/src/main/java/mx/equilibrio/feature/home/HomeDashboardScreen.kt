package mx.equilibrio.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.report.ReportSection
import mx.equilibrio.ui.components.EqAccountCard
import mx.equilibrio.ui.components.EqAccountCardHeight
import mx.equilibrio.ui.components.EqAccountFamily
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqButtonVariant
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqFab
import mx.equilibrio.ui.components.EqSectionHeader
import mx.equilibrio.ui.components.EqSkeleton
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget
import mx.equilibrio.ui.theme.color
import mx.equilibrio.ui.theme.softColor

/** Inicio: tarjetas, saldo, últimos movimientos y reportes destacados. */
@Composable
fun HomeDashboardScreen(
    onAddClicked: () -> Unit,
    onCardsClicked: () -> Unit,
    onCardClicked: (String) -> Unit,
    onAddCardClicked: () -> Unit,
    onBalanceClicked: () -> Unit,
    onSeeMoreClicked: () -> Unit,
    onReportsClicked: () -> Unit,
    onReportClicked: (ReportSection) -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = {
            val title = state.greetingName?.let { "Hola, $it" } ?: "Hola"
            EqTopBar(title = title, trailing = trailing)
        },
        floatingActionButton = { EqFab(onClick = onAddClicked) },
    ) { padding ->
        if (state.isLoading) {
            EqSkeleton(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        // Sin padding horizontal en la lista: los carruseles llegan al borde para que se asome la siguiente tarjeta.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + Spacing.sm,
                bottom = Spacing.xxxl + Spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item(key = "cards_header") {
                EqSectionHeader("Tarjetas", onClick = onCardsClicked, modifier = SectionPadding)
            }
            item(key = "cards") {
                if (state.cards.isEmpty()) {
                    EqEmptyState(
                        title = "Aún no tienes tarjetas",
                        body = "Agrega tu efectivo, cuenta o tarjeta de crédito para ver cuánto tienes disponible.",
                        icon = Icons.Rounded.CreditCard,
                        action = {
                            EqButton(
                                text = "Agregar cuenta",
                                onClick = onAddCardClicked,
                                variant = EqButtonVariant.SECONDARY,
                                fullWidth = false,
                            )
                        },
                    )
                } else {
                    CardsCarousel(cards = state.cards, onCardClicked = onCardClicked)
                }
            }

            item(key = "balance_header") {
                EqSectionHeader("Saldo", onClick = onBalanceClicked, modifier = SectionPadding.padding(top = Spacing.sm))
            }
            item(key = "balance") {
                Box(SectionPadding) { BalanceHeaderCard(state.balanceCents) }
            }

            item(key = "recent_header") {
                Text(
                    text = "Últimas transacciones",
                    style = EquilibrioTheme.typography.h3,
                    color = EquilibrioTheme.colors.ink,
                    modifier = SectionPadding.padding(top = Spacing.base, bottom = Spacing.xs),
                )
            }
            item(key = "recent") {
                RecentTransactions(
                    transactions = state.recentTransactions,
                    onSeeMoreClicked = onSeeMoreClicked,
                    modifier = SectionPadding,
                )
            }

            item(key = "reports_header") {
                EqSectionHeader("Reportes", onClick = onReportsClicked, modifier = SectionPadding.padding(top = Spacing.sm))
            }
            item(key = "reports") {
                if (state.reportHighlights.isEmpty()) {
                    EqEmptyState(
                        title = "Aún no hay reportes",
                        body = "Registra movimientos este mes para ver tus reportes aquí.",
                        icon = Icons.Rounded.PieChart,
                    )
                } else {
                    ReportsCarousel(highlights = state.reportHighlights, onReportClicked = onReportClicked)
                }
            }
        }
    }
}

private val SectionPadding = Modifier.padding(horizontal = Spacing.base)

/** Carrusel con snap; el padding final deja asomar el borde de la siguiente página. */
@Composable
private fun <T> PeekCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    page: @Composable (T) -> Unit,
) {
    val pagerState = rememberPagerState { items.size }
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = Spacing.base, end = Spacing.xxxl),
        pageSpacing = Spacing.md,
        verticalAlignment = Alignment.Top,
    ) { index ->
        page(items[index])
    }
}

@Composable
private fun CardsCarousel(cards: List<HomeCardUi>, onCardClicked: (String) -> Unit) {
    PeekCarousel(items = cards) { card ->
        EqAccountCard(
            title = card.typeLabel,
            maskedDigits = card.maskedDigits,
            availableCents = card.availableCents,
            family = card.family,
            colorSlot = card.colorSlot,
            dueDay = card.dueDay,
            onClick = { onCardClicked(card.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(EqAccountCardHeight),
        )
    }
}

@Composable
private fun RecentTransactions(
    transactions: List<TransactionUi>,
    onSeeMoreClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (transactions.isEmpty()) {
        EqEmptyState(
            title = "Aún no registras movimientos",
            body = "Registra tu primer ingreso o gasto para ver tu equilibrio.",
            modifier = modifier,
        )
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        transactions.forEach { transaction ->
            TransactionRow(transaction = transaction, onClick = onSeeMoreClicked)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TouchTarget.minSize)
                .clip(ShapeMedium)
                .clickable(role = Role.Button, onClick = onSeeMoreClicked),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Ver más", style = EquilibrioTheme.typography.label, color = EquilibrioTheme.colors.inkMuted)
            Spacer(Modifier.size(Spacing.sm))
            Icon(Icons.Rounded.MoreHoriz, contentDescription = null, tint = EquilibrioTheme.colors.inkMuted)
        }
    }
}

@Composable
private fun ReportsCarousel(highlights: List<ReportHighlightUi>, onReportClicked: (ReportSection) -> Unit) {
    PeekCarousel(items = highlights) { highlight ->
        ReportHighlightCard(highlight = highlight, onClick = { onReportClicked(highlight.section) })
    }
}

@Composable
private fun ReportHighlightCard(highlight: ReportHighlightUi, onClick: () -> Unit) {
    val colors = EquilibrioTheme.colors
    EqCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(ReportCardHeight),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(highlight.tone.softColor(colors), ShapeMedium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = highlight.section.icon(),
                    contentDescription = null,
                    tint = highlight.tone.color(colors),
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlight.title,
                    style = EquilibrioTheme.typography.h3,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = highlight.description,
                    style = EquilibrioTheme.typography.bodySmall,
                    color = colors.inkMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

private val ReportCardHeight = 120.dp

private fun ReportSection.icon(): ImageVector = when (this) {
    ReportSection.SUMMARY -> Icons.Rounded.Insights
    ReportSection.TREND -> Icons.Rounded.BarChart
    ReportSection.MATRIX -> Icons.Rounded.GridView
    ReportSection.CATEGORIES -> Icons.Rounded.PieChart
}

@Preview(showBackground = true)
@Composable
private fun CardsCarouselPreview() {
    EquilibrioTheme {
        CardsCarousel(
            cards = listOf(
                HomeCardUi("1", "Efectivo", "****", 320_00, EqAccountFamily.GREEN, 0, null),
                HomeCardUi("2", "Crédito", "•••• 4821", 8_500_00, EqAccountFamily.PURPLE, 1, 12),
            ),
            onCardClicked = {},
        )
    }
}
