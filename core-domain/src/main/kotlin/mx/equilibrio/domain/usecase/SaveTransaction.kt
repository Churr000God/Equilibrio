package mx.equilibrio.domain.usecase

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.requireNoFutureConfirmedDate
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Gasto/Ingreso "normal" con selector de cuenta libre en QuickEntry no puede
 * apuntar a una CREDIT_CARD — eso saltearía todo el period tracking/límite
 * (ver [SaveCreditPurchase], el único camino admitido para EXPENSE contra
 * tarjeta). No aplica a [CreateTransfer]/[PayCreditPeriod]: esos arman sus
 * `Transaction` a mano y escriben vía `TransactionRepository.upsert`
 * directamente, sin pasar por este caso de uso.
 */
class SaveTransaction @Inject constructor(
    private val repository: TransactionRepository,
    private val getAccount: GetAccount,
) {
    suspend operator fun invoke(transaction: Transaction, today: LocalDate) {
        val account = getAccount(transaction.accountId)
        require(account == null || account.type != AccountType.CREDIT_CARD) {
            "No se puede guardar un ${transaction.kind} directo contra una cuenta CREDIT_CARD; usá SaveCreditPurchase."
        }

        requireNoFutureConfirmedDate(transaction.status, transaction.occurredAt, today)

        val existing = repository.getById(transaction.id)
        require(existing == null || existing.status != TransactionStatus.COMPLETED || transaction.occurredAt <= today) {
            "No se puede editar una transacción ya confirmada para ponerle fecha futura."
        }

        repository.upsert(transaction)
    }
}
