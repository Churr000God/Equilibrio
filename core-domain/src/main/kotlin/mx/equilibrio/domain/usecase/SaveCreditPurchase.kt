package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.deriveTransactionStatus
import mx.equilibrio.domain.model.requireWithinCreditLimit
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Reemplaza a [SaveTransaction] para EXPENSE en cuentas CREDIT_CARD. El chequeo
 * de límite disponible vive acá (no en la UI) con `require`, para que ninguna
 * pantalla — presente o futura — pueda saltearse el control. Es el caso N=1 de
 * [requireWithinCreditLimit] (misma regla que usa [SaveInstallmentPurchase]).
 */
class SaveCreditPurchase @Inject constructor(
    private val getAccount: GetAccount,
    private val getOrCreatePeriodForDate: GetOrCreatePeriodForDate,
    private val periodRepository: PeriodRepository,
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
        val account = requireCreditCardAccount(getAccount, accountId)
        val creditLimitCents = requireNotNull(account.creditLimitCents)

        val period = getOrCreatePeriodForDate(accountId, occurredAt)

        val existing = transactionRepository.getById(id)
        require(existing == null || existing.status != TransactionStatus.COMPLETED || occurredAt <= today) {
            "No se puede editar una compra ya confirmada para ponerle fecha futura."
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

        val periods = periodRepository.observeByAccount(accountId).first()
        val existingCharges = transactionRepository.observeAll().first().filter { it.id != id }
        requireWithinCreditLimit(creditLimitCents, periods, existingCharges, listOf(transaction), today)

        transactionRepository.upsert(transaction)
        return transaction
    }
}
