package mx.equilibrio.feature.goals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqButtonVariant
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqChip
import mx.equilibrio.ui.components.EqDestructiveDialog
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqProgressBar
import mx.equilibrio.ui.components.EqProgressRing
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeLarge
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.ShapeSmall
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget
import mx.equilibrio.ui.theme.eqShadow
import mx.equilibrio.ui.theme.tabularAmountStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onAddClicked: () -> Unit,
    onGoalClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: Dp = Spacing.xxl,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = EquilibrioTheme.colors

    state.pendingDeletion?.let { pending ->
        EqDestructiveDialog(
            title = "¿Eliminar '${pending.name}'?",
            body = "Esto no se puede deshacer. Los abonos que ya hiciste se quedan en tu historial.",
            confirmLabel = "Eliminar",
            onConfirm = { viewModel.onEvent(GoalsEvent.DeleteConfirmed) },
            onDismiss = { viewModel.onEvent(GoalsEvent.DeleteCancelled) },
        )
    }

    state.contribute?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(GoalsEvent.ContributeDismissed) },
            containerColor = colors.surface,
        ) {
            ContributeSheet(sheet = sheet, onEvent = viewModel::onEvent)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            EqTopBar(
                title = "Tus metas",
                subtitle = if (state.totalSavedCents > 0) "Has guardado ${formatCents(state.totalSavedCents)} en total" else null,
                trailing = {
                    IconButton(
                        onClick = onAddClicked,
                        modifier = Modifier
                            .size(TouchTarget.minSize)
                            .background(colors.green, ShapeMedium),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Nueva meta", tint = Color.White)
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding))
            state.isEmpty -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EqEmptyState(
                    title = "Aún no tienes metas",
                    body = "Ponle nombre a algo que quieras lograr y ve cómo avanza con cada abono.",
                    action = { EqButton(text = "Crear mi primera meta", onClick = onAddClicked, fullWidth = false) },
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.base,
                    end = Spacing.base,
                    top = padding.calculateTopPadding() + Spacing.sm,
                    bottom = bottomInset,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.base),
            ) {
                state.featured?.let { featured ->
                    item {
                        FeaturedGoalCard(
                            goal = featured,
                            onContribute = { viewModel.onEvent(GoalsEvent.ContributeRequested(featured.id)) },
                            onClick = { onGoalClicked(featured.id) },
                            onLongClick = { viewModel.onEvent(GoalsEvent.DeleteRequested(featured)) },
                        )
                    }
                }

                if (state.others.isNotEmpty()) {
                    item { SectionTitle("Otras metas") }
                    items(state.others, key = { it.id }) { goal ->
                        GoalRow(
                            goal = goal,
                            onClick = { onGoalClicked(goal.id) },
                            onLongClick = { viewModel.onEvent(GoalsEvent.DeleteRequested(goal)) },
                            onContribute = { viewModel.onEvent(GoalsEvent.ContributeRequested(goal.id)) },
                        )
                    }
                }

                item { StreakCard(weeks = state.streakWeeks) }

                if (state.completed.isNotEmpty()) {
                    item { SectionTitle("Logradas") }
                    items(state.completed, key = { it.id }) { goal ->
                        GoalRow(
                            goal = goal,
                            onClick = { onGoalClicked(goal.id) },
                            onLongClick = { viewModel.onEvent(GoalsEvent.DeleteRequested(goal)) },
                            onContribute = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = EquilibrioTheme.typography.h2, color = EquilibrioTheme.colors.ink)
}

/** Meta destacada: tarjeta hero en verde profundo con anillo y abono rápido. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedGoalCard(
    goal: GoalUi,
    onContribute: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colors = EquilibrioTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .eqShadow(Elevation.LEVEL_2, ShapeLarge)
            .background(colors.greenDeep, ShapeLarge)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.base)) {
            EqProgressRing(
                progress = goal.progress,
                tone = DomainTone.ESSENTIAL,
                fillColor = Color.White,
                trackColor = Color.White.copy(alpha = 0.22f),
                size = 84.dp,
            ) {
                Text(
                    text = "${(goal.progress * 100).roundToInt()}%",
                    style = tabularAmountStyle(fontSize = 18.sp),
                    color = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, style = EquilibrioTheme.typography.h2, color = Color.White)
                Text(
                    text = "${formatCents(goal.savedCents)} de ${formatCents(goal.targetCents)}",
                    style = tabularAmountStyle(fontSize = 14.sp, weight = androidx.compose.ui.text.font.FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.padding(top = 2.dp),
                )
                goal.deadline?.let { deadline ->
                    Text(
                        text = "${deadline.monthYearLabel()} · ${deadlineHint(deadline, today())}",
                        style = EquilibrioTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.base))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, ShapeMedium)
                .combinedClickable(onClick = onContribute)
                .padding(vertical = Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.greenDeep, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.xs))
            Text("Abonar", style = EquilibrioTheme.typography.bodyStrong, color = colors.greenDeep)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GoalRow(
    goal: GoalUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onContribute: (() -> Unit)?,
) {
    val colors = EquilibrioTheme.colors
    EqCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Box(
                modifier = Modifier.size(40.dp).background(colors.greenSoft, ShapeSmall),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (goal.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Savings,
                    contentDescription = null,
                    tint = colors.greenDeep,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
                val deadline = goal.deadline?.let { " · ${it.monthYearLabel()}" } ?: " · Sin fecha"
                Text(
                    text = "${formatCents(goal.savedCents)} de ${formatCents(goal.targetCents)}$deadline",
                    style = EquilibrioTheme.typography.caption,
                    color = colors.inkMuted,
                )
            }
            Text(
                text = "${(goal.progress * 100).roundToInt()}%",
                style = tabularAmountStyle(fontSize = 14.sp),
                color = if (goal.isCompleted) colors.greenDeep else colors.purple,
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        EqProgressBar(progress = goal.progress, tone = if (goal.isCompleted) DomainTone.ESSENTIAL else DomainTone.RECREATIONAL)
        if (onContribute != null) {
            Spacer(Modifier.height(Spacing.sm))
            EqButton(text = "Abonar", onClick = onContribute, variant = EqButtonVariant.SECONDARY, fullWidth = false)
        }
    }
}

/** Racha en morado suave: es dato de disciplina, no un juicio. */
@Composable
private fun StreakCard(weeks: Int) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.purpleSoft, ShapeMedium)
            .padding(Spacing.base),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(colors.purple, ShapeSmall),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(streakLabel(weeks), style = EquilibrioTheme.typography.bodyStrong, color = colors.purpleDeep)
            Text(streakBody(weeks), style = EquilibrioTheme.typography.bodySmall, color = colors.inkMuted)
        }
    }
}

@Composable
private fun ContributeSheet(sheet: ContributeUi, onEvent: (GoalsEvent) -> Unit) {
    val colors = EquilibrioTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.base)
            .padding(bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.base),
    ) {
        Text("Abonar a '${sheet.goalName}'", style = EquilibrioTheme.typography.h2, color = colors.ink)
        EqTextField(
            value = sheet.amountInput,
            onValueChange = { onEvent(GoalsEvent.ContributeAmountChanged(it)) },
            label = "Monto",
            keyboardType = KeyboardType.Decimal,
            isError = sheet.amountError != null,
            helperOrError = sheet.amountError,
        )
        if (sheet.accounts.size > 1) {
            Text("Sale de", style = EquilibrioTheme.typography.label, color = colors.inkMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                sheet.accounts.forEach { account ->
                    EqChip(
                        label = account.name,
                        selected = account.id == sheet.accountId,
                        onClick = { onEvent(GoalsEvent.ContributeAccountChanged(account.id)) },
                    )
                }
            }
        }
        EqButton(
            text = "Confirmar abono",
            onClick = { onEvent(GoalsEvent.ContributeConfirmed) },
            enabled = sheet.canConfirm,
            loading = sheet.isSaving,
        )
    }
}
