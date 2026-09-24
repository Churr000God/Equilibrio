package mx.equilibrio.feature.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.CategoryIcons
import mx.equilibrio.ui.components.EqAmount
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqButtonVariant
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqDestructiveDialog
import mx.equilibrio.ui.components.EqProgressBar
import mx.equilibrio.ui.components.EqSkeleton
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.color

@Composable
fun TransactionDetailScreen(
    onEditClicked: (String) -> Unit,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStopRecurringConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onClosed()
    }

    if (showDeleteConfirm) {
        // Sin rama para isRecurringOccurrence acá: es un movimiento normal, mismo diálogo que
        // cualquier otro — borrar UNA ocurrencia nunca toca la plantilla ni las demás.
        EqDestructiveDialog(
            title = if (state.isInstallment) "¿Eliminar esta compra a meses?" else "¿Eliminar este movimiento?",
            body = if (state.isInstallment) {
                "Se borrarán las ${state.movementsToDeleteCount} cuotas de esta compra."
            } else {
                "No podrás deshacer esta acción."
            },
            confirmLabel = "Eliminar",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.onEvent(TransactionDetailEvent.DeleteConfirmed)
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }

    if (showStopRecurringConfirm) {
        EqDestructiveDialog(
            title = "¿Dejar de repetir este movimiento?",
            body = "No se van a generar más ocurrencias. Los movimientos ya registrados se quedan como están.",
            confirmLabel = "Dejar de repetir",
            onConfirm = {
                showStopRecurringConfirm = false
                viewModel.onEvent(TransactionDetailEvent.StopRecurringConfirmed)
            },
            onDismiss = { showStopRecurringConfirm = false },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(
            title = "Detalle",
            onBack = onClosed,
            trailing = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones", tint = EquilibrioTheme.colors.ink)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = EquilibrioTheme.colors.error) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = EquilibrioTheme.colors.error) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirm = true
                            },
                        )
                    }
                }
            },
        )

        when {
            state.isLoading -> EqSkeleton(modifier = Modifier.weight(1f), rowHeight = 72.dp)

            !state.found -> Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Este movimiento ya no existe.", style = EquilibrioTheme.typography.body, color = EquilibrioTheme.colors.inkMuted)
            }

            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.base)
                    .clipToBounds()
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                AmountHeaderSection(state = state)

                InfoCard(state = state)

                state.plan?.let { plan -> InstallmentPlanSection(plan = plan) }

                if (state.isRecurringOccurrence) {
                    RecurringSection(
                        label = state.recurrenceLabel,
                        isActive = state.isRecurringActive,
                        isPausing = state.isPausingRecurring,
                        onStopClicked = { showStopRecurringConfirm = true },
                    )
                }

                if (!state.note.isNullOrBlank()) {
                    EqCard {
                        Text(text = "Nota", style = EquilibrioTheme.typography.bodyStrong, color = EquilibrioTheme.colors.ink)
                        Text(
                            text = state.note!!,
                            style = EquilibrioTheme.typography.body,
                            color = EquilibrioTheme.colors.inkMuted,
                            modifier = Modifier.padding(top = Spacing.xs),
                        )
                    }
                }

                // Sin condición extra para isRecurringOccurrence: a diferencia de una cuota, una
                // ocurrencia recurrente es un movimiento normal y editable como cualquier otro.
                if (!state.isInstallment) {
                    EqButton(
                        text = "Editar",
                        onClick = { onEditClicked(state.id) },
                        variant = EqButtonVariant.SECONDARY,
                        enabled = !state.isDeleting,
                        modifier = Modifier.padding(vertical = Spacing.base),
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountHeaderSection(state: TransactionDetailUiState) {
    val colors = EquilibrioTheme.colors
    val tone = if (state.isSavings) DomainTone.ESSENTIAL else if (state.isExpense) DomainTone.RECREATIONAL else DomainTone.INCOME_FIXED
    val tint = tone.color(colors)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(56.dp).background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val icon = state.categoryIcon?.let { CategoryIcons.get(it) }
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
            } else {
                Text(text = (state.categoryName ?: state.typeLabel).first().uppercase(), style = EquilibrioTheme.typography.h2, color = tint)
            }
        }
        EqAmount(
            amountCents = state.amountCents,
            tone = tone,
            showSign = true,
            isNegative = state.isExpense,
            fontSize = 34.sp,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        val subtitle = if (state.isInstallment) {
            "Cuota ${state.installmentIndex} de ${state.installmentCount}" + (state.note?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")
        } else {
            state.classificationLabel + (state.note?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")
        }
        Text(text = subtitle, style = EquilibrioTheme.typography.body, color = colors.inkMuted, modifier = Modifier.padding(top = Spacing.xs))
        StatusBadge(label = state.statusLabel, isScheduled = state.isScheduled)
    }
}

@Composable
private fun StatusBadge(label: String, isScheduled: Boolean) {
    val colors = EquilibrioTheme.colors
    val background = if (isScheduled) colors.neutralBadge else colors.greenSoft
    val foreground = if (isScheduled) colors.inkMuted else colors.greenDeep
    Box(
        modifier = Modifier
            .padding(top = Spacing.sm)
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs / 2),
    ) {
        Text(text = label, style = EquilibrioTheme.typography.caption, color = foreground)
    }
}

@Composable
private fun InfoCard(state: TransactionDetailUiState) {
    EqCard {
        InfoRow(label = "Fecha", value = state.occurredAt?.longLabel() ?: "—")
        InfoRow(label = "Cuenta", value = state.accountName ?: "—")
        if (!state.isSavings) {
            InfoRow(label = "Categoría", value = state.categoryName ?: "Sin categoría")
        }
        InfoRow(label = "Tipo", value = state.typeLabel, isLast = true)
    }
}

@Composable
private fun InfoRow(label: String, value: String, isLast: Boolean = false) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = EquilibrioTheme.typography.body, color = colors.inkMuted)
        Text(text = value, style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
    }
}

@Composable
private fun RecurringSection(label: String?, isActive: Boolean, isPausing: Boolean, onStopClicked: () -> Unit) {
    val colors = EquilibrioTheme.colors
    EqCard {
        Text(text = label ?: "Recurrente", style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
        if (isActive) {
            EqButton(
                text = "Dejar de repetir",
                onClick = onStopClicked,
                variant = EqButtonVariant.SECONDARY,
                enabled = !isPausing,
                loading = isPausing,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        } else {
            Text(
                text = "Ya no se van a generar más ocurrencias.",
                style = EquilibrioTheme.typography.caption,
                color = colors.inkMuted,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

@Composable
private fun InstallmentPlanSection(plan: InstallmentPlanUi) {
    val colors = EquilibrioTheme.colors
    EqCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Plan de cuotas", style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
            Text(text = "${plan.currentIndex} de ${plan.count}", style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
        }
        EqProgressBar(progress = plan.progress, tone = DomainTone.RECREATIONAL, modifier = Modifier.padding(top = Spacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = "Restante", style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
                Text(text = formatCents(plan.remainingCents), style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
            }
            if (plan.nextDueDateLabel != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Próxima cuota", style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
                    Text(text = plan.nextDueDateLabel, style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
                }
            }
        }
    }
}
