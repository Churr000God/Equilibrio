package mx.equilibrio.domain.usecase

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.deriveTransactionStatus
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Pago de un periodo de tarjeta: EGRESO real en [sourceAccountId] (CASH/BANK) +
 * incremento de `amountPaidCents` del periodo activo. No genera una transacción
 * simétrica de "ingreso" en la tarjeta — pagar deuda no es income. Un sobrepago
 * queda como saldo a favor natural al cerrar el periodo (carriedBalanceCents
 * negativo en [SettleDuePeriods]).
 */
class PayCreditPeriod @Inject constructor(
    private val getAccount: GetAccount,
    private val periodRepository: PeriodRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        cardAccountId: String,
        sourceAccountId: String,
        amountCents: Long,
        occurredAt: LocalDate,
        today: LocalDate,
    ): Transaction {
        val cardAccount = getAccount(cardAccountId)
        requireNotNull(cardAccount) { "No existe la cuenta $cardAccountId" }
        require(cardAccount.type == AccountType.CREDIT_CARD) {
            "PayCreditPeriod solo aplica a cuentas CREDIT_CARD, no ${cardAccount.type}"
        }
        val sourceAccount = getAccount(sourceAccountId)
        requireNotNull(sourceAccount) { "No existe la cuenta $sourceAccountId" }
        require(sourceAccount.type in TRANSFERABLE_TYPES) {
            "La cuenta origen debe ser CASH o BANK, no se puede pagar desde ${sourceAccount.type}"
        }

        val active = periodRepository.getActiveByAccount(cardAccountId).firstOrNull()
        requireNotNull(active) { "La tarjeta $cardAccountId no tiene un periodo activo" }

        val payment = Transaction(
            id = UUID.randomUUID().toString(),
            userId = sourceAccount.userId,
            accountId = sourceAccountId,
            kind = TransactionKind.EXPENSE,
            classification = null,
            amountCents = amountCents,
            occurredAt = occurredAt,
            note = null,
            categoryId = null,
            status = deriveTransactionStatus(occurredAt, today),
            periodId = null,
        )
        transactionRepository.upsert(payment)
        periodRepository.upsert(active.copy(amountPaidCents = active.amountPaidCents + amountCents))
        return payment
    }
}
