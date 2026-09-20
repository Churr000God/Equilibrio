package mx.equilibrio.domain.usecase

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.Transfer
import mx.equilibrio.domain.model.deriveTransactionStatus
import mx.equilibrio.domain.repository.TransferRepository
import javax.inject.Inject

private val TRANSFERABLE_TYPES = setOf(AccountType.CASH, AccountType.BANK)

class CreateTransfer @Inject constructor(
    private val getAccount: GetAccount,
    private val transferRepository: TransferRepository,
) {
    suspend operator fun invoke(
        originAccountId: String,
        destinationAccountId: String,
        amountCents: Long,
        occurredAt: LocalDate,
        note: String?,
        userId: String,
        expenseTransactionId: String,
        incomeTransactionId: String,
        today: LocalDate,
    ): Transfer {
        require(originAccountId != destinationAccountId) {
            "originAccountId y destinationAccountId no pueden ser la misma cuenta"
        }

        val origin = getAccount(originAccountId)
        requireNotNull(origin) { "No existe la cuenta origen $originAccountId" }
        val destination = getAccount(destinationAccountId)
        requireNotNull(destination) { "No existe la cuenta destino $destinationAccountId" }

        require(origin.type in TRANSFERABLE_TYPES) {
            "La cuenta origen debe ser CASH o BANK, no se puede transferir desde ${origin.type}"
        }
        require(destination.type in TRANSFERABLE_TYPES) {
            "La cuenta destino debe ser CASH o BANK, no se puede transferir hacia ${destination.type}"
        }

        val status = deriveTransactionStatus(occurredAt, today)

        val expense = Transaction(
            id = expenseTransactionId,
            userId = userId,
            accountId = originAccountId,
            kind = TransactionKind.EXPENSE,
            classification = null,
            amountCents = amountCents,
            occurredAt = occurredAt,
            note = note,
            categoryId = null,
            status = status,
        )
        val income = Transaction(
            id = incomeTransactionId,
            userId = userId,
            accountId = destinationAccountId,
            kind = TransactionKind.INCOME,
            classification = null,
            amountCents = amountCents,
            occurredAt = occurredAt,
            note = note,
            categoryId = null,
            status = status,
        )

        return transferRepository.create(expense, income)
    }
}
