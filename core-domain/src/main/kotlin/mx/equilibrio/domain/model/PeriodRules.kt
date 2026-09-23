package mx.equilibrio.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * `statementDay`/`dueDay` viajan junto a los bounds calculados (no solo las
 * fechas resultantes) porque [nextCycleBounds] los necesita para recalcular el
 * siguiente ciclo desde cero contra el día objetivo original. Encadenar a
 * partir del día-del-mes ya clampeado de [endAt] arrastraría el clamp para
 * siempre (ej. un corte día 31 que cayó en 28 por febrero nunca volvería a 31
 * en marzo).
 */
data class PeriodBounds(
    val startAt: LocalDate,
    val endAt: LocalDate,
    val payAt: LocalDate,
    val statementDay: Int,
    val dueDay: Int,
)

/** Bounds del ciclo de corte/pago que contiene [referenceDate]. */
fun deriveCycleBounds(statementDay: Int, dueDay: Int, referenceDate: LocalDate): PeriodBounds {
    require(statementDay in 1..31) { "statementDay debe estar entre 1 y 31, fue $statementDay" }
    require(dueDay in 1..31) { "dueDay debe estar entre 1 y 31, fue $dueDay" }

    var end = clampedDate(referenceDate.year, referenceDate.monthNumber, statementDay)
    if (end < referenceDate) {
        end = addMonthsClamped(end, 1, statementDay)
    }
    val start = addMonthsClamped(end, -1, statementDay).plus(1, DateTimeUnit.DAY)

    var payAt = clampedDate(end.year, end.monthNumber, dueDay)
    if (payAt <= end) {
        payAt = addMonthsClamped(payAt, 1, dueDay)
    }

    return PeriodBounds(startAt = start, endAt = end, payAt = payAt, statementDay = statementDay, dueDay = dueDay)
}

/** Bounds del ciclo inmediatamente siguiente a [previous], encadenado sin arrastrar clamps. */
fun nextCycleBounds(previous: PeriodBounds): PeriodBounds =
    deriveCycleBounds(previous.statementDay, previous.dueDay, previous.endAt.plus(1, DateTimeUnit.DAY))

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> error("Mes inválido: $month")
}

private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

private fun clampedDate(year: Int, month: Int, day: Int): LocalDate =
    LocalDate(year, month, day.coerceAtMost(daysInMonth(year, month)))

internal fun addMonthsClamped(date: LocalDate, months: Int, targetDay: Int): LocalDate {
    val totalMonths = (date.monthNumber - 1) + months
    val year = date.year + Math.floorDiv(totalMonths, 12)
    val month = Math.floorMod(totalMonths, 12) + 1
    return clampedDate(year, month, targetDay)
}
