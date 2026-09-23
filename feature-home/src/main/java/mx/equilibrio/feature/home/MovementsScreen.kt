package mx.equilibrio.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqAlertBanner
import mx.equilibrio.ui.components.EqAmount
import mx.equilibrio.ui.components.EqBadge
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqDestructiveDialog
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqFab
import mx.equilibrio.ui.components.EqSkeleton
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget

/** Inicio anterior: saldo y todos los movimientos. Se abre desde "Ver más" en el Inicio. */
@Composable
fun MovementsScreen(
    onAddClicked: () -> Unit,
    onTransactionClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.pendingDeletion?.let { pending ->
        EqDestructiveDialog(
            title = if (pending.isInstallment) "¿Eliminar esta compra a meses?" else "¿Eliminar este movimiento?",
            body = if (pending.isInstallment) {
                "Se borrarán las ${pending.installmentCount} cuotas de esta compra."
            } else {
                "${pending.label} de ${formatCents(pending.amountCents)} del ${pending.occurredAt}."
            },
            confirmLabel = "Eliminar",
            onConfirm = { viewModel.onEvent(HomeEvent.DeleteConfirmed) },
            onDismiss = { viewModel.onEvent(HomeEvent.DeleteCancelled) },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = {
            val title = state.greetingName?.let { "Hola, $it" } ?: "Hola"
            EqTopBar(title = title, trailing = trailing)
        },
        floatingActionButton = { EqFab(onClick = onAddClicked) },
    ) { padding ->
        when {
            state.isLoading -> EqSkeleton(modifier = Modifier.padding(padding))
            state.isEmpty -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EqEmptyState(
                    title = "Aún no registras movimientos",
                    body = "Registra tu primer ingreso o gasto para ver tu equilibrio.",
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.base,
                    end = Spacing.base,
                    top = padding.calculateTopPadding() + Spacing.sm,
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
                item {
                    BalanceHeaderCard(state.balanceCents)
                }
                items(state.transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = {
                            if (!transaction.isSavings && !transaction.isInstallment) {
                                onTransactionClicked(transaction.id)
                            }
                        },
                        onDeleteRequested = { viewModel.onEvent(HomeEvent.DeleteRequested(transaction)) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun BalanceHeaderCard(balanceCents: Long) {
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TransactionRow(
    transaction: TransactionUi,
    onClick: () -> Unit,
    onDeleteRequested: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TouchTarget.minRowHeight)
            .combinedClickable(onClick = onClick, onLongClick = onDeleteRequested)
            .background(EquilibrioTheme.colors.surface, ShapeMedium)
            .padding(horizontal = Spacing.base),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        EqBadge(text = transaction.label, tone = transaction.tone, icon = domainToneIcon(transaction.tone))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note?.takeIf { it.isNotBlank() } ?: transaction.label,
                style = EquilibrioTheme.typography.body,
                color = EquilibrioTheme.colors.ink,
            )
            Text(
                text = transaction.occurredAt.toString(),
                style = EquilibrioTheme.typography.caption,
                color = EquilibrioTheme.colors.inkFaint,
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

/** Refuerzo con ícono para el modo accesibilidad (EqBadge solo lo pinta si está activo). */
internal fun domainToneIcon(tone: DomainTone): ImageVector = when (tone) {
    DomainTone.INCOME_FIXED -> Icons.Rounded.Repeat
    DomainTone.INCOME_VARIABLE -> Icons.Rounded.TrendingUp
    DomainTone.ESSENTIAL -> Icons.Rounded.CheckCircle
    DomainTone.RECREATIONAL -> Icons.Rounded.Star
    DomainTone.NEUTRAL -> Icons.Rounded.HelpOutline
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
