package mx.equilibrio.feature.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.report.CategoryShare
import mx.equilibrio.domain.model.report.CrossMatrix
import mx.equilibrio.domain.model.report.MatrixInsight
import mx.equilibrio.domain.model.report.MonthlyTrendPoint
import mx.equilibrio.domain.model.report.PeriodComparison
import mx.equilibrio.domain.model.report.ReportSection
import mx.equilibrio.ui.components.BarPair
import mx.equilibrio.ui.components.EqAmount
import mx.equilibrio.ui.components.EqBarChart
import mx.equilibrio.ui.components.EqBarChartLegend
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqIllustration
import mx.equilibrio.ui.components.EqMatrixCell
import mx.equilibrio.ui.components.EqProgressBar
import mx.equilibrio.ui.components.EqStatTile
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    bottomInset: Dp = Spacing.xxl,
    focusSection: ReportSection? = null,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = EquilibrioTheme.colors
    val listState = rememberLazyListState()
    val sections = state.report?.let { report ->
        ReportSection.entries.filter { it != ReportSection.CATEGORIES || report.categories.isNotEmpty() }
    }.orEmpty()

    // Llegar desde Inicio con una sección elegida: se baja hasta ella una sola vez, cuando ya hay datos.
    LaunchedEffect(focusSection, sections.isNotEmpty()) {
        val index = sections.indexOf(focusSection)
        if (index > 0) listState.animateScrollToItem(index)
    }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        EqTopBar(
            title = "Tu resumen",
            trailing = {
                MonthSelector(
                    label = state.month.label(),
                    canGoForward = state.canGoForward,
                    onPrevious = { viewModel.onEvent(ReportsEvent.PreviousMonth) },
                    onNext = { viewModel.onEvent(ReportsEvent.NextMonth) },
                )
            },
        )

        val report = state.report
        when {
            state.isLoading || report == null -> Box(Modifier.weight(1f).fillMaxWidth())
            state.isEmpty -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                EqEmptyState(
                    title = "Aún no hay movimientos en ${state.month.label().lowercase()}",
                    body = "Registra ingresos y gastos y aquí verás cómo se mueve tu mes.",
                    illustration = EqIllustration.NEUTRAL,
                )
            }

            else -> LazyColumn(
                // clipToBounds(): el stretch de overscroll no debe pintar fuera de sus bounds.
                modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds(),
                state = listState,
                contentPadding = PaddingValues(
                    start = Spacing.base,
                    end = Spacing.base,
                    top = Spacing.sm,
                    bottom = bottomInset,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.base),
            ) {
                sections.forEach { section ->
                    item(key = section) {
                        when (section) {
                            ReportSection.SUMMARY -> SummaryTiles(report.comparison)
                            ReportSection.TREND -> TrendCard(report.trend)
                            ReportSection.MATRIX -> MatrixCard(report.matrix, report.insight)
                            ReportSection.CATEGORIES -> CategoriesCard(report.categories)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(label: String, canGoForward: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = Modifier.background(colors.surface, ShapePill),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mes anterior", tint = colors.ink)
        }
        Text(label, style = EquilibrioTheme.typography.label, color = colors.ink)
        IconButton(onClick = onNext, enabled = canGoForward, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "Mes siguiente",
                tint = if (canGoForward) colors.ink else colors.border,
            )
        }
    }
}

@Composable
private fun SummaryTiles(comparison: PeriodComparison) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        EqStatTile("Ingresos", comparison.current.incomeCents, comparison.incomeDelta, Modifier.weight(1f))
        EqStatTile("Gastos", comparison.current.expenseCents, comparison.expenseDelta, Modifier.weight(1f))
        EqStatTile("Te quedó", comparison.current.leftoverCents, comparison.leftoverDelta, Modifier.weight(1f))
    }
}

@Composable
private fun TrendCard(trend: List<MonthlyTrendPoint>) {
    val colors = EquilibrioTheme.colors
    EqCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Últimos 6 meses", style = EquilibrioTheme.typography.h3, color = colors.ink, modifier = Modifier.weight(1f))
            EqBarChartLegend()
        }
        Spacer(Modifier.height(Spacing.base))
        EqBarChart(
            bars = trend.map { BarPair(it.month.shortLabel(), it.incomeCents, it.expenseCents) },
        )
    }
}

@Composable
private fun MatrixCard(matrix: CrossMatrix, insight: MatrixInsight) {
    val colors = EquilibrioTheme.colors
    EqCard(modifier = Modifier.fillMaxWidth()) {
        Text("De dónde sale cada gasto", style = EquilibrioTheme.typography.h3, color = colors.ink)
        Text(
            "Tu ingreso fijo y variable cruzado con lo esencial y lo recreativo.",
            style = EquilibrioTheme.typography.bodySmall,
            color = colors.inkMuted,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        Spacer(Modifier.height(Spacing.base))

        if (matrix.isEmpty) {
            Text(insight.text(), style = EquilibrioTheme.typography.bodySmall, color = colors.inkMuted)
            return@EqCard
        }

        val labelWidth = 92.dp
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(labelWidth))
            MatrixHeader("Esencial", Modifier.weight(1f))
            Spacer(Modifier.width(Spacing.sm))
            MatrixHeader("Recreativo", Modifier.weight(1f))
        }
        Spacer(Modifier.height(Spacing.sm))
        MatrixRow("Ingreso fijo", labelWidth, matrix.fixedEssential, matrix.fixedRecreational, matrix)
        Spacer(Modifier.height(Spacing.sm))
        MatrixRow("Ingreso variable", labelWidth, matrix.variableEssential, matrix.variableRecreational, matrix)

        Spacer(Modifier.height(Spacing.base))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.greenSoft, ShapeMedium)
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = colors.greenDeep, modifier = Modifier.size(18.dp))
            Text(insight.text(), style = EquilibrioTheme.typography.bodySmall, color = colors.greenDeep)
        }
    }
}

@Composable
private fun MatrixHeader(text: String, modifier: Modifier) {
    Text(
        text,
        style = EquilibrioTheme.typography.caption,
        color = EquilibrioTheme.colors.inkMuted,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun MatrixRow(label: String, labelWidth: Dp, essential: Long, recreational: Long, matrix: CrossMatrix) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = EquilibrioTheme.typography.caption,
            color = EquilibrioTheme.colors.inkMuted,
            modifier = Modifier.width(labelWidth),
        )
        EqMatrixCell(essential, matrix.shareOf(essential), DomainTone.ESSENTIAL, Modifier.weight(1f))
        Spacer(Modifier.width(Spacing.sm))
        EqMatrixCell(recreational, matrix.shareOf(recreational), DomainTone.RECREATIONAL, Modifier.weight(1f))
    }
}

@Composable
private fun CategoriesCard(categories: List<CategoryShare>) {
    val colors = EquilibrioTheme.colors
    EqCard(modifier = Modifier.fillMaxWidth()) {
        Text("En qué se te va más", style = EquilibrioTheme.typography.h3, color = colors.ink)
        Spacer(Modifier.height(Spacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            categories.take(MAX_CATEGORIES).forEachIndexed { index, share ->
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            share.name ?: "Sin categoría",
                            style = EquilibrioTheme.typography.body,
                            color = colors.ink,
                            modifier = Modifier.weight(1f),
                        )
                        EqAmount(amountCents = share.cents, tone = if (index == 0) DomainTone.ESSENTIAL else DomainTone.NEUTRAL)
                    }
                    Spacer(Modifier.height(Spacing.xs))
                    EqProgressBar(progress = share.share, tone = if (index == 0) DomainTone.ESSENTIAL else DomainTone.INCOME_VARIABLE)
                }
            }
        }
    }
}

private const val MAX_CATEGORIES = 5
