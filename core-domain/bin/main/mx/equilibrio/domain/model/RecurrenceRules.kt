package mx.equilibrio.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Próxima ocurrencia a partir de [current]. MONTHLY reusa [addMonthsClamped], no deriva el ancla. */
fun nextOccurrence(current: LocalDate, frequency: RecurrenceFrequency, anchorDay: Int?): LocalDate = when (frequency) {
    RecurrenceFrequency.WEEKLY -> current.plus(7, DateTimeUnit.DAY)
    RecurrenceFrequency.BIWEEKLY -> current.plus(14, DateTimeUnit.DAY)
    RecurrenceFrequency.MONTHLY -> addMonthsClamped(current, 1, requireNotNull(anchorDay) { "MONTHLY requiere anchorDay" })
}

/** Ancla de una serie MONTHLY (día de [startAt]); null para las demás frecuencias. */
fun anchorDayFor(startAt: LocalDate, frequency: RecurrenceFrequency): Int? =
    if (frequency == RecurrenceFrequency.MONTHLY) startAt.day else null
