package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

data class Period(
    val id: String,
    val accountId: String,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val payAt: LocalDate,
    val state: PeriodState,
    val carriedBalanceCents: Long,
    val amountPaidCents: Long,
)
