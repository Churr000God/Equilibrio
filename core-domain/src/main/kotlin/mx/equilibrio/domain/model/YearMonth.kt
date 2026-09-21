package mx.equilibrio.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Mes calendario. kotlinx-datetime no lo trae en la versión usada. */
data class YearMonth(val year: Int, val month: Int) : Comparable<YearMonth> {
    init {
        require(month in 1..12) { "month debe estar en 1..12, fue $month" }
    }

    fun firstDay(): LocalDate = LocalDate(year, month, 1)

    fun lastDay(): LocalDate = firstDay().plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

    fun previous(): YearMonth = if (month == 1) YearMonth(year - 1, 12) else YearMonth(year, month - 1)

    fun next(): YearMonth = if (month == 12) YearMonth(year + 1, 1) else YearMonth(year, month + 1)

    fun minusMonths(n: Int): YearMonth = (1..n).fold(this) { acc, _ -> acc.previous() }

    operator fun contains(date: LocalDate): Boolean = date.year == year && date.monthNumber == month

    override fun compareTo(other: YearMonth): Int = compareValuesBy(this, other, { it.year }, { it.month })

    companion object {
        fun of(date: LocalDate): YearMonth = YearMonth(date.year, date.monthNumber)
    }
}
