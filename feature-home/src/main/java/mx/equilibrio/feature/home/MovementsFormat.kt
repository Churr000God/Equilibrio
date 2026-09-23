package mx.equilibrio.feature.home

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import mx.equilibrio.domain.model.YearMonth

private val WEEKDAYS = mapOf(
    DayOfWeek.MONDAY to "Lunes",
    DayOfWeek.TUESDAY to "Martes",
    DayOfWeek.WEDNESDAY to "Miércoles",
    DayOfWeek.THURSDAY to "Jueves",
    DayOfWeek.FRIDAY to "Viernes",
    DayOfWeek.SATURDAY to "Sábado",
    DayOfWeek.SUNDAY to "Domingo",
)

private val MONTHS = listOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic",
)

/** "Hoy" / "Ayer" / "Lunes 21 sep" — mismo criterio del mock (designs/transacciones.png). */
fun LocalDate.dayGroupLabel(today: LocalDate): String = when {
    this == today -> "Hoy"
    this == today.minus(1, DateTimeUnit.DAY) -> "Ayer"
    else -> "${WEEKDAYS.getValue(dayOfWeek)} $dayOfMonth ${MONTHS[monthNumber - 1]}"
}

/** "25 oct 2026". */
fun LocalDate.longLabel(): String = "$dayOfMonth ${MONTHS[monthNumber - 1]} $year"

private val FULL_MONTHS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

/** "Septiembre 2026" — mismo criterio que en :feature-categories. */
fun YearMonth.label(): String = "${FULL_MONTHS[month - 1].replaceFirstChar { it.uppercase() }} $year"
