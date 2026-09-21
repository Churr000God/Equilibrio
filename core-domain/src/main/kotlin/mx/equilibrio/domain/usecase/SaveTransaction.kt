package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Transaction
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
    suspend operator fun invoke(transaction: Transaction) {
        val account = getAccount(transaction.accountId)
        require(account == null || account.type != AccountType.CREDIT_CARD) {
            "No se puede guardar un ${transaction.kind} directo contra una cuenta CREDIT_CARD; usá SaveCreditPurchase."
        }
        repository.upsert(transaction)
    }
}
