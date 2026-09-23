package mx.equilibrio.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.CategoryIcons
import mx.equilibrio.ui.components.EqAlertBanner
import mx.equilibrio.ui.components.EqAmount
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqFab
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqSkeleton
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget
import mx.equilibrio.ui.theme.color

private enum class MovementsTab(val label: String) {
    ALL("Todos"), EXPENSES("Gastos"), INCOME("Ingresos"), SCHEDULED("Programadas")
}

/** Todos los movimientos del mes en curso, con detalle propio. Se abre desde "Ver más" en Inicio o la pestaña Transacciones. */
@Composable
fun MovementsScreen(
    onAddClicked: () -> Unit,
    onTransactionClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(MovementsTab.ALL) }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(title = "Transacciones", trailing = trailing)

        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading) {
                EqSkeleton()
            } else {
                // El selector de mes siempre tiene que estar visible, incluso si el mes elegido no
                // tiene movimientos — si no, el usuario queda sin forma de volver a un mes con datos.
                val shown = when (selectedTab) {
                    MovementsTab.ALL -> state.transactions
                    MovementsTab.EXPENSES -> state.transactions.filter { it.isExpense }
                    MovementsTab.INCOME -> state.transactions.filter { !it.isExpense }
                    MovementsTab.SCHEDULED -> state.transactions.filter { it.isScheduled }
                }
                val groups = shown.groupBy { it.occurredAt }

                LazyColumn(
                    // clipToBounds(): sin esto el stretch de overscroll invade el área del topBar.
                    modifier = Modifier.fillMaxSize().clipToBounds(),
                    contentPadding = PaddingValues(
                        start = Spacing.base,
                        end = Spacing.base,
                        top = Spacing.sm,
                        bottom = Spacing.xxl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.base),
                ) {
                    items(state.pendingAlerts, key = { it.id }) { alert ->
                        EqAlertBanner(
                            message = alert.message,
                            onDismiss = { viewModel.onEvent(HomeEvent.AlertDismissed(alert.id)) },
                        )
                    }
                    item(key = "month_selector") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            MonthSelector(
                                label = state.month.label(),
                                canGoForward = state.canGoForward,
                                onPrevious = { viewModel.onEvent(HomeEvent.PreviousMonth) },
                                onNext = { viewModel.onEvent(HomeEvent.NextMonth) },
                            )
                        }
                    }
                    item(key = "balance_header") {
                        BalanceHeaderCard(
                            balanceCents = state.balanceCents,
                            incomeCents = state.monthIncomeCents,
                            expenseCents = state.monthExpenseCents,
                        )
                    }
                    item(key = "tabs") {
                        EqSegmentedControl(
                            options = MovementsTab.entries.map { it.label },
                            selectedIndex = selectedTab.ordinal,
                            onSelect = { index -> selectedTab = MovementsTab.entries[index] },
                        )
                    }

                    if (shown.isEmpty()) {
                        item(key = "tab_empty") {
                            EqEmptyState(
                                title = if (selectedTab == MovementsTab.ALL) "Sin movimientos este mes" else "Sin movimientos en esta vista",
                                body = "Cambiá de mes con el selector de arriba o registrá un ingreso o gasto.",
                            )
                        }
                    }

                    groups.forEach { (date, transactions) ->
                        item(key = "header_$date") {
                            Text(
                                text = date.dayGroupLabel(today()),
                                style = EquilibrioTheme.typography.bodySmall,
                                color = EquilibrioTheme.colors.inkMuted,
                            )
                        }
                        items(transactions, key = { it.id }) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                onClick = {
                                    if (!transaction.isSavings) onTransactionClicked(transaction.id)
                                },
                            )
                        }
                    }
                }
            }

            EqFab(onClick = onAddClicked, modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg))
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
            modifier = Modifier.padding(horizontal = Spacing.xs),
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

@Composable
internal fun BalanceHeaderCard(balanceCents: Long, incomeCents: Long = 0, expenseCents: Long = 0) {
    EqCard(hero = true, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Saldo disponible",
            style = EquilibrioTheme.typography.label,
            color = EquilibrioTheme.colors.inkMuted,
        )
        EqAmount(
            amountCents = balanceCents,
            tone = if (balanceCents < 0) DomainTone.RECREATIONAL else DomainTone.ESSENTIAL,
            fontSize = 40.sp,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            Column {
                Text(text = "Ingresos", style = EquilibrioTheme.typography.caption, color = EquilibrioTheme.colors.inkMuted)
                EqAmount(amountCents = incomeCents, tone = DomainTone.INCOME_FIXED, showSign = true, fontSize = 16.sp)
            }
            Column {
                Text(text = "Gastos", style = EquilibrioTheme.typography.caption, color = EquilibrioTheme.colors.inkMuted)
                EqAmount(amountCents = expenseCents, tone = DomainTone.RECREATIONAL, showSign = true, isNegative = true, fontSize = 16.sp)
            }
        }
    }
}

@Composable
internal fun TransactionRow(
    transaction: TransactionUi,
    onClick: () -> Unit,
) {
    val colors = EquilibrioTheme.colors
    val tint = transaction.tone.color(colors)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.minRowHeight)
            .clickable(onClick = onClick)
            .background(colors.surface, ShapeMedium)
            .padding(horizontal = Spacing.base, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            val icon = if (transaction.isScheduled) Icons.Rounded.Schedule else transaction.categoryIcon?.let { CategoryIcons.get(it) }
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = (transaction.categoryName ?: transaction.label).firstOrNull()?.uppercase() ?: "?",
                    style = EquilibrioTheme.typography.label,
                    color = tint,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note?.takeIf { it.isNotBlank() } ?: transaction.categoryName ?: transaction.label,
                style = EquilibrioTheme.typography.body,
                color = colors.ink,
            )
            Text(
                text = if (transaction.isScheduled) {
                    "Programada · vence ${transaction.occurredAt.longLabel()}"
                } else {
                    listOfNotNull(transaction.categoryName ?: transaction.label, transaction.accountName).joinToString(" · ")
                },
                style = EquilibrioTheme.typography.caption,
                color = colors.inkFaint,
            )
        }
        EqAmount(
            amountCents = transaction.amountCents,
            tone = transaction.tone,
            showSign = true,
            isNegative = transaction.isExpense,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovementsScreenEmptyPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            EqEmptyState(
                title = "Aún no registras movimientos",
                body = "Registra tu primer ingreso o gasto para ver tu equilibrio.",
            )
        }
    }
}
