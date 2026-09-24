package mx.equilibrio.domain.usecase

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.repository.RecurringTransactionRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Confirma la transacción (delegado simple, ver KDoc de
 * [mx.equilibrio.domain.repository.TransactionRepository.confirm] para la
 * atomicidad de transferencias). Un movimiento con fecha futura no se puede
 * confirmar: debe esperar a su fecha (issue #6). Si la
 * transacción confirmada es una ocurrencia recurrente activa, dispara ahí
 * mismo la generación de la siguiente ocurrencia como SCHEDULED — el
 * disparador es la confirmación manual, no el paso del tiempo. Una
 * transferencia nunca tiene `recurringSeriesId`, así que ese caso cae solo.
 */
class ConfirmTransaction @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val generateDueRecurringTransactions: GenerateDueRecurringTransactions,
) {
    suspend operator fun invoke(id: String, today: LocalDate) {
        val pending = transactionRepository.getById(id) ?: return
        require(pending.occurredAt <= today) { FUTURE_DATE_MESSAGE }
        transactionRepository.confirm(id, today)
        val confirmed = transactionRepository.getById(id) ?: return
        val seriesId = confirmed.recurringSeriesId ?: return
        val series = recurringTransactionRepository.getById(seriesId) ?: return
        if (!series.isActive) return
        generateDueRecurringTransactions.materializeNext(series, today)
    }

    companion object {
        const val FUTURE_DATE_MESSAGE = "No se puede confirmar con fecha futura"
    }
}
