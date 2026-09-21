package mx.equilibrio.domain.calc

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Racha de ahorro: semanas (lunes a domingo) consecutivas con al menos un
 * abono, contando hacia atrás desde la semana actual. Si la semana actual
 * aún no tiene abono, la racha sigue viva si la anterior sí lo tuvo.
 */
object SavingStreakCalculator {

    fun weeks(contributionDates: List<LocalDate>, today: LocalDate): Int {
        if (contributionDates.isEmpty()) return 0

        val weeksWithContribution = contributionDates.map { it.weekStart() }.toHashSet()
        val currentWeek = today.weekStart()

        var cursor = if (currentWeek in weeksWithContribution) currentWeek else currentWeek.minus(7, DateTimeUnit.DAY)
        var streak = 0
        while (cursor in weeksWithContribution) {
            streak++
            cursor = cursor.minus(7, DateTimeUnit.DAY)
        }
        return streak
    }

    private fun LocalDate.weekStart(): LocalDate {
        val offset = dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal
        return minus(offset, DateTimeUnit.DAY)
    }
}
