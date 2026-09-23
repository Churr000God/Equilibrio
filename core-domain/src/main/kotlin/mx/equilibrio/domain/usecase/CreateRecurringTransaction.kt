package mx.equilibrio.domain.usecase

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.anchorDayFor
import mx.equilibrio.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class CreateRecurringTransaction @Inject constructor(
    private val repository: RecurringTransactionRepository,
    private val getAccount: GetAccount,
    private val generateDueRecurringTransactions: GenerateDueRecurringTransactions,
) {
    suspend operator fun invoke(
        id: String,
        userId: String,
        accountId: String,
        kind: TransactionKind,
        classification: Classification,
        amountCents: Long,
        note: String?,
        categoryId: String?,
        frequency: RecurrenceFrequency,
        startAt: LocalDate,
        today: LocalDate,
    ): RecurringTransaction {
        val account = getAccount(accountId)
        require(account == null || account.type != AccountType.CREDIT_CARD) {
            "No se puede crear una transacción recurrente contra una cuenta CREDIT_CARD; usá una compra a meses."
        }

        val series = RecurringTransaction(
            id = id,
            userId = userId,
            accountId = accountId,
            kind = kind,
            classification = classification,
            amountCents = amountCents,
            note = note,
            categoryId = categoryId,
            frequency = frequency,
            anchorDay = anchorDayFor(startAt, frequency),
            nextOccurrenceAt = startAt,
            isActive = true,
            createdAt = today,
        )
        repository.upsert(series)
        generateDueRecurringTransactions(today)
        return series
    }
}
