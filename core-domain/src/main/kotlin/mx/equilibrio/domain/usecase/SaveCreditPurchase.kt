package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.deriveTransactionStatus
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Reemplaza a [SaveTransaction] para EXPENSE en cuentas CREDIT_CARD. El chequeo
 * de límite disponible vive acá (no en la UI) con `require`, para que ninguna
 * pantalla — presente o futura — pueda saltearse el control.
 */
class SaveCreditPurchase @Inject constructor(
    private val getAccount: GetAccount,
    private val getOrCreatePeriodForDate: GetOrCreatePeriodForDate,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        id: String,
        accountId: String,
        classification: Classification?,
        amountCents: Long,
        occurredAt: LocalDate,
        note: String?,
        categoryId: String?,
        userId: String,
        today: LocalDate,
    ): Transaction {
        val account = getAccount(accountId)
        requireNotNull(account) { "No existe la cuenta $accountId" }
        require(account.type == AccountType.CREDIT_CARD) {
            "SaveCreditPurchase solo aplica a cuentas CREDIT_CARD, no ${account.type}"
        }
        val creditLimitCents = requireNotNull(account.creditLimitCents) {
            "La cuenta $accountId no tiene creditLimitCents configurado"
        }

        val period = getOrCreatePeriodForDate(accountId, occurredAt)

        val spentInPeriod = transactionRepository.observeAll().first()
            .filter { it.periodId == period.id && it.kind == TransactionKind.EXPENSE }
            .sumOf { it.amountCents }

        val availableCents = creditLimitCents - (period.carriedBalanceCents + spentInPeriod - period.amountPaidCents)
        require(amountCents <= availableCents) {
            "La compra de $amountCents excede el disponible de la tarjeta ($availableCents)"
        }

        val transaction = Transaction(
            id = id,
            userId = userId,
            accountId = accountId,
            kind = TransactionKind.EXPENSE,
            classification = classification,
            amountCents = amountCents,
            occurredAt = occurredAt,
            note = note,
            categoryId = categoryId,
            status = deriveTransactionStatus(occurredAt, today),
            periodId = period.id,
        )
        transactionRepository.upsert(transaction)
        return transaction
    }
}
