package mx.equilibrio.feature.goals

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

private val MONTHS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

fun LocalDate.monthYearLabel(): String = "${MONTHS[monthNumber - 1].replaceFirstChar { it.uppercase() }} $year"

fun LocalDate.shortLabel(): String = "$dayOfMonth de ${MONTHS[monthNumber - 1]} de $year"

/** "quedan 12 semanas" / "queda 1 semana" / "esta semana" / "plazo vencido". Coaching, no juicio. */
fun deadlineHint(deadline: LocalDate, today: LocalDate): String {
    val days = today.daysUntil(deadline)
    return when {
        days < 0 -> "plazo vencido"
        days < 7 -> "esta semana"
        days / 7 == 1 -> "queda 1 semana"
        else -> "quedan ${days / 7} semanas"
    }
}

fun streakLabel(weeks: Int): String = when (weeks) {
    0 -> "Empieza tu racha"
    1 -> "1 semana ahorrando"
    else -> "$weeks semanas ahorrando seguido"
}

fun streakBody(weeks: Int): String =
    if (weeks == 0) "Guarda algo esta semana y arrancamos la cuenta." else "Guarda algo esta semana para no romper la racha."
