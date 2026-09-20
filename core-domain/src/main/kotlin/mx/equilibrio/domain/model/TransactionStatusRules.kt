package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

fun deriveTransactionStatus(occurredAt: LocalDate, today: LocalDate): TransactionStatus =
    if (occurredAt > today) TransactionStatus.SCHEDULED else TransactionStatus.COMPLETED
