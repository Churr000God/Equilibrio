package mx.equilibrio.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.ui.components.CategoryIcons
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqButtonVariant
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqProgressBar
import mx.equilibrio.ui.components.EqSkeleton
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

@Composable
fun CategoryDetailScreen(
    onEditClicked: (String) -> Unit,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onClosed()
    }

    if (showDeleteConfirm) {
        CategoryDeleteDialog(
            categoryName = state.name,
            movementsCount = state.movementsCount,
            onReassign = {
                showDeleteConfirm = false
                viewModel.onEvent(CategoryDetailEvent.DeleteConfirmed(reassignToOtros = true))
            },
            onDeleteAll = {
                showDeleteConfirm = false
                viewModel.onEvent(CategoryDetailEvent.DeleteConfirmed(reassignToOtros = false))
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(title = state.name.ifBlank { "Categoría" }, onBack = onClosed)

        if (state.isLoading) {
            EqSkeleton(modifier = Modifier.weight(1f), rowHeight = 72.dp)
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.base)
                .clipToBounds()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            CategoryHeaderSection(state = state)

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                DetailStatCard(label = "Este mes", value = formatCents(state.monthTotalCents), modifier = Modifier.weight(1f))
                DetailStatCard(label = "Movimientos", value = state.movementsCount.toString(), modifier = Modifier.weight(1f))
                DetailStatCard(label = "Promedio", value = formatCents(state.averageCents), modifier = Modifier.weight(1f))
            }

            if (state.monthlyBudgetCents != null) {
                BudgetSection(state = state)
            }

            RecentTransactionsSection(transactions = state.recentTransactions)

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(vertical = Spacing.base)) {
                EqButton(
                    text = "Eliminar",
                    variant = EqButtonVariant.DESTRUCTIVE_LOW_EMPHASIS,
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                )
                EqButton(
                    text = "Editar",
                    onClick = { onEditClicked(state.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CategoryHeaderSection(state: CategoryDetailUiState) {
    val colors = EquilibrioTheme.colors
    val tint = categoryColor(state.type, state.colorSlot, colors)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val icon = CategoryIcons.get(state.icon)
            if (icon != null) {
                androidx.compose.material3.Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
            } else {
                Text(text = state.name.firstOrNull()?.uppercase() ?: "?", style = EquilibrioTheme.typography.h1, color = tint)
            }
        }
        Text(
            text = state.name,
            style = EquilibrioTheme.typography.h1,
            color = colors.ink,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), modifier = Modifier.padding(top = Spacing.xs)) {
            DetailTag(text = state.typeLabel, background = if (state.type == CategoryType.INCOME) colors.greenSoft else colors.purpleSoft, foreground = if (state.type == CategoryType.INCOME) colors.greenDeep else colors.purpleDeep)
            DetailTag(text = "Activa", background = colors.greenSoft, foreground = colors.greenDeep)
        }
    }
}

@Composable
private fun DetailTag(text: String, background: androidx.compose.ui.graphics.Color, foreground: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs / 2),
    ) {
        Text(text = text, style = EquilibrioTheme.typography.caption, color = foreground)
    }
}

@Composable
private fun DetailStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = EquilibrioTheme.colors
    EqCard(modifier = modifier) {
        Text(text = label, style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
        Text(
            text = value,
            style = EquilibrioTheme.typography.bodyStrong,
            color = colors.ink,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun BudgetSection(state: CategoryDetailUiState) {
    val colors = EquilibrioTheme.colors
    val budget = state.monthlyBudgetCents ?: return
    val remaining = state.budgetRemainingCents ?: 0
    val tone = if (state.type == CategoryType.INCOME) DomainTone.INCOME_FIXED else DomainTone.ESSENTIAL

    EqCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Presupuesto mensual", style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
            Text(
                text = "${formatCents(state.monthTotalCents)} de ${formatCents(budget)}",
                style = EquilibrioTheme.typography.caption,
                color = colors.inkMuted,
            )
        }
        EqProgressBar(
            progress = state.budgetProgress ?: 0f,
            tone = tone,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        Text(
            text = if (remaining >= 0) "Te quedan ${formatCents(remaining)} para este mes" else "Te pasaste ${formatCents(-remaining)} este mes",
            style = EquilibrioTheme.typography.caption,
            color = colors.inkMuted,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun RecentTransactionsSection(transactions: List<RecentTransactionUi>) {
    val colors = EquilibrioTheme.colors
    Column {
        Text(text = "Últimos movimientos", style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
        if (transactions.isEmpty()) {
            Text(
                text = "Sin movimientos este mes.",
                style = EquilibrioTheme.typography.caption,
                color = colors.inkMuted,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        } else {
            Column(modifier = Modifier.padding(top = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                transactions.forEach { tx ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(text = tx.note, style = EquilibrioTheme.typography.body, color = colors.ink)
                            Text(
                                text = listOf(tx.dateLabel, tx.accountLabel).filter { it.isNotBlank() }.joinToString(" · "),
                                style = EquilibrioTheme.typography.caption,
                                color = colors.inkMuted,
                            )
                        }
                        Text(text = formatCents(tx.amountCents), style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
                    }
                }
            }
        }
    }
}
