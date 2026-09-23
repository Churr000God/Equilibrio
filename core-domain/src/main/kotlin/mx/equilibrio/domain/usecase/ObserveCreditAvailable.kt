package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.CreditAvailability
import mx.equilibrio.domain.model.computeCreditAvailability
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Espejo de [ObserveAvailableBalances] pero para CREDIT_CARD: real y
 * proyectado vía [computeCreditAvailability]. Combina periodos por cuenta con
 * `periodRepository.observeByAccount` (no `getActiveByAccount` puntual) para
 * que un pago o cierre de periodo también dispare una reemisión, no solo un
 * cambio en transacciones. [ObserveAvailableBalances] no se toca — sigue
 * excluyendo CREDIT_CARD, es una métrica distinta.
 */
class ObserveCreditAvailable @Inject constructor(
    private val accountRepository: AccountRepository,
    private val periodRepository: PeriodRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(today: LocalDate): Flow<Map<String, CreditAvailability>> =
        combine(accountRepository.observeAll(), transactionRepository.observeAll()) { accounts, transactions ->
            accounts.filter { it.type == AccountType.CREDIT_CARD } to transactions
        }.flatMapLatest { (creditCardAccounts, transactions) ->
            if (creditCardAccounts.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(creditCardAccounts.map { periodRepository.observeByAccount(it.id) }) { periodsByAccount ->
                    creditCardAccounts.zip(periodsByAccount.toList()).mapNotNull { (account, periods) ->
                        val creditLimitCents = account.creditLimitCents ?: return@mapNotNull null
                        val charges = transactions.filter { it.accountId == account.id }
                        account.id to computeCreditAvailability(creditLimitCents, periods, charges, today)
                    }.toMap()
                }
            }
        }
}
