package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

fun deriveTransactionStatus(occurredAt: LocalDate, today: LocalDate): TransactionStatus =
    if (occurredAt > today) TransactionStatus.SCHEDULED else TransactionStatus.COMPLETED

/**
 * Ninguna transacción CONFIRMADA puede tener fecha futura — ni al crearla
 * directamente como confirmada, ni al editar una que ya estaba confirmada
 * (eso la "regresaría" a programada, lo cual no es una edición válida).
 */
fun requireNoFutureConfirmedDate(status: TransactionStatus, occurredAt: LocalDate, today: LocalDate) {
    require(status != TransactionStatus.COMPLETED || occurredAt <= today) {
        "Una transacción CONFIRMADA no puede tener fecha futura ($occurredAt es posterior a $today)"
    }
}
