package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.deriveTransactionStatus
import mx.equilibrio.domain.model.nextOccurrence
import mx.equilibrio.domain.repository.RecurringTransactionRepository
import mx.equilibrio.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Catch-up perezoso, mismo patrón que [SettleDuePeriods] pero por usuario, no
 * por cuenta: las series recurrentes abarcan todas las cuentas CASH/BANK.
 * Procesa la serie activa vencida más antigua repetidamente, para ponerse al
 * día en una sola llamada aunque hayan quedado varios ciclos sin generar.
 *
 * `@Singleton` + [Mutex]: a diferencia de [SettleDuePeriods] (un solo call
 * site), esta va a tener dos (Inicio y Transacciones) que pueden construirse
 * casi simultáneamente — sin el mutex, ambos podrían generar la misma
 * ocurrencia duplicada.
 */
@Singleton
class GenerateDueRecurringTransactions @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val getAccount: GetAccount,
) {
    private val mutex = Mutex()

    suspend operator fun invoke(today: LocalDate) = mutex.withLock {
        while (true) {
            val due = recurringRepository.observeAll().first()
                .filter { it.isActive && it.nextOccurrenceAt <= today }
                .minByOrNull { it.nextOccurrenceAt } ?: return@withLock
            generateOne(due, today)
        }
    }

    /**
     * Guardia crítica: si la cuenta ya no existe o es CREDIT_CARD, desactiva la
     * serie y devuelve — nunca "saltarla" sin mutar nada, o el `while(true)` de
     * [invoke] gira infinito sobre la misma serie vencida.
     */
    internal suspend fun generateOne(series: RecurringTransaction, today: LocalDate) {
        val account = getAccount(series.accountId)
        if (account == null || account.type == AccountType.CREDIT_CARD) {
            recurringRepository.upsert(series.copy(isActive = false))
            return
        }

        transactionRepository.upsert(
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = series.userId,
                accountId = series.accountId,
                kind = series.kind,
                classification = series.classification,
                amountCents = series.amountCents,
                occurredAt = series.nextOccurrenceAt,
                note = series.note,
                categoryId = series.categoryId,
                status = deriveTransactionStatus(series.nextOccurrenceAt, today),
                recurringSeriesId = series.id,
            ),
        )
        recurringRepository.upsert(
            series.copy(nextOccurrenceAt = nextOccurrence(series.nextOccurrenceAt, series.frequency, series.anchorDay)),
        )
    }
}
