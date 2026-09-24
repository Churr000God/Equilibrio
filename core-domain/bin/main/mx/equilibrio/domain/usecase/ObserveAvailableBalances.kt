package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Saldo real por cuenta CASH/BANK: `balanceCents` guardado + neto de movimientos
 * COMPLETED. Las CREDIT_CARD quedan fuera — su disponible se calcula distinto,
 * ver [ObserveCreditAvailable]. Lo SCHEDULED no cuenta acá porque todavía no
 * impactó el saldo real (para el proyectado ver [ObserveScheduledCashFlowCents]).
 */
class ObserveAvailableBalances @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<Map<String, Long>> =
        combine(accountRepository.observeAll(), transactionRepository.observeAll()) { accounts, transactions ->
            accounts
                .filter { it.type == AccountType.CASH || it.type == AccountType.BANK }
                .associate { account ->
                    val net = transactions
                        .filter { it.accountId == account.id && it.status == TransactionStatus.COMPLETED }
                        .sumOf { if (it.kind == TransactionKind.INCOME) it.amountCents else -it.amountCents }
                    account.id to (account.balanceCents + net)
                }
        }
}
