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
 * Red de seguridad, no catch-up: por cada serie activa que TODAVÍA no generó
 * ninguna ocurrencia (típicamente recién creada), materializa la primera.
 * Series con al menos una ocurrencia ya generada (SCHEDULED o COMPLETED) no
 * se tocan — de ahí en adelante, la siguiente ocurrencia se dispara al
 * confirmar la anterior a mano, ver [ConfirmTransaction]. No hay auto-confirm
 * por fecha vencida: el usuario confirma, el sistema reacciona a eso.
 *
 * `@Singleton` + [Mutex]: dos call sites (Inicio y Transacciones) pueden
 * construirse casi simultáneamente — sin el mutex, ambos podrían generar la
 * misma primera ocurrencia duplicada.
 */
@Singleton
class GenerateDueRecurringTransactions @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val getAccount: GetAccount,
) {
    private val mutex = Mutex()

    suspend operator fun invoke(today: LocalDate) = mutex.withLock {
        recurringRepository.observeAll().first()
            .filter { it.isActive }
            .forEach { series ->
                val hasAnyOccurrence = transactionRepository.observeByRecurringSeries(series.id).first().isNotEmpty()
                if (!hasAnyOccurrence) materializeNext(series, today)
            }
    }

    /**
     * Materializa la ocurrencia en `series.nextOccurrenceAt` (status derivado
     * de la fecha vía [deriveTransactionStatus]) y avanza la serie. Reusado
     * por [ConfirmTransaction] para armar la siguiente ocurrencia justo al
     * confirmar la anterior. Guardia crítica: si la cuenta ya no existe o es
     * CREDIT_CARD, desactiva la serie en su lugar y no genera nada.
     */
    internal suspend fun materializeNext(series: RecurringTransaction, today: LocalDate) {
        val account = getAccount(series.accountId)
        if (account == null || account.type == AccountType.CREDIT_CARD) {
            recurringRepository.upsert(series.copy(isActive = false))
            return
        }

        val occurredAt = series.nextOccurrenceAt
        transactionRepository.upsert(
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = series.userId,
                accountId = series.accountId,
                kind = series.kind,
                classification = series.classification,
                amountCents = series.amountCents,
                occurredAt = occurredAt,
                note = series.note,
                categoryId = series.categoryId,
                status = deriveTransactionStatus(occurredAt, today),
                recurringSeriesId = series.id,
            ),
        )
        recurringRepository.upsert(
            series.copy(nextOccurrenceAt = nextOccurrence(occurredAt, series.frequency, series.anchorDay)),
        )
    }
}
