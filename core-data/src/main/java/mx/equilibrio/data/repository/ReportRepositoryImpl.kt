package mx.equilibrio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.dao.CategoryRow
import mx.equilibrio.data.local.dao.MonthlyRow
import mx.equilibrio.data.local.dao.PeriodTotalRow
import mx.equilibrio.data.local.dao.ReportDao
import mx.equilibrio.data.mapper.toEpochMillis
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.model.report.CategoryShare
import mx.equilibrio.domain.model.report.MonthlyTrendPoint
import mx.equilibrio.domain.model.report.PeriodTotals
import mx.equilibrio.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val dao: ReportDao,
    private val session: LocalSession,
) : ReportRepository {

    override fun observeTotals(month: YearMonth): Flow<PeriodTotals> = flow {
        val (from, to) = month.range()
        emitAll(dao.observePeriodTotals(session.currentUserId(), from, to).map { it.toPeriodTotals() })
    }

    override fun observeTrend(months: Int, until: YearMonth): Flow<List<MonthlyTrendPoint>> = flow {
        val window = (months - 1 downTo 0).map { until.minusMonths(it) }
        val from = window.first().firstDay().toEpochMillis()
        val to = until.lastDay().toEpochMillis()
        emitAll(dao.observeMonthlyTrend(session.currentUserId(), from, to).map { rows -> rows.toTrend(window) })
    }

    override fun observeCategoryBreakdown(month: YearMonth): Flow<List<CategoryShare>> = flow {
        val (from, to) = month.range()
        emitAll(dao.observeCategoryTotals(session.currentUserId(), from, to).map { it.toShares() })
    }

    override fun observeSavingsCents(month: YearMonth): Flow<Long> = flow {
        val (from, to) = month.range()
        emitAll(dao.observeSavingsCents(session.currentUserId(), from, to))
    }

    private fun YearMonth.range(): Pair<Long, Long> = firstDay().toEpochMillis() to lastDay().toEpochMillis()
}

// El DAO devuelve una fila por combinación (kind, classification) ya sumada en SQL; acá solo
// se distribuye cada fila al bucket de PeriodTotals que le corresponde. La clasificación manda
// sobre el kind (fixed/variable/essential/recreational); solo cuando no hay clasificación se
// cae a "unclassified" según sea ingreso o gasto.
internal fun List<PeriodTotalRow>.toPeriodTotals(): PeriodTotals {
    var totals = PeriodTotals()
    for (row in this) {
        val kind = TransactionKind.valueOf(row.kind)
        val classification = row.classification?.let { Classification.valueOf(it) }
        totals = when {
            classification == Classification.FIXED -> totals.copy(fixedIncomeCents = row.totalCents)
            classification == Classification.VARIABLE -> totals.copy(variableIncomeCents = row.totalCents)
            classification == Classification.ESSENTIAL -> totals.copy(essentialExpenseCents = row.totalCents)
            classification == Classification.RECREATIONAL -> totals.copy(recreationalExpenseCents = row.totalCents)
            kind == TransactionKind.INCOME -> totals.copy(unclassifiedIncomeCents = row.totalCents)
            else -> totals.copy(unclassifiedExpenseCents = row.totalCents)
        }
    }
    return totals
}

// window trae todos los meses de la ventana aunque no tengan movimientos (p. ej. un mes sin
// actividad); byMonth[key].orEmpty() rellena esos huecos con 0 en vez de omitir el punto,
// para que la tendencia no tenga meses faltantes en el eje.
internal fun List<MonthlyRow>.toTrend(window: List<YearMonth>): List<MonthlyTrendPoint> {
    val byMonth = groupBy { it.month }
    return window.map { month ->
        val key = "%04d-%02d".format(month.year, month.month)
        val rows = byMonth[key].orEmpty()
        MonthlyTrendPoint(
            month = month,
            incomeCents = rows.filter { it.kind == TransactionKind.INCOME.name }.sumOf { it.totalCents },
            expenseCents = rows.filter { it.kind == TransactionKind.EXPENSE.name }.sumOf { it.totalCents },
        )
    }
}

internal fun List<CategoryRow>.toShares(): List<CategoryShare> {
    // coerceAtLeast(1) evita división entre 0 cuando el mes no tuvo gasto en ninguna categoría;
    // en ese caso share queda en 0 para todas en vez de NaN/crash.
    val total = sumOf { it.totalCents }.coerceAtLeast(1)
    return map { row ->
        CategoryShare(
            categoryId = row.categoryId,
            name = row.categoryName,
            cents = row.totalCents,
            share = row.totalCents.toFloat() / total,
        )
    }
}
