package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Espejo de [ObserveAvailableBalances] pero para CREDIT_CARD: disponible sobre
 * saldo revolvente = credit_limit - (carriedBalanceCents del periodo activo +
 * gastado_del_periodo - amountPaidCents). El arrastre ya encadena la deuda de
 * periodos anteriores, así que es una lectura O(1) del periodo activo, no una
 * suma sobre todo el historial. [ObserveAvailableBalances] no se toca — sigue
 * excluyendo CREDIT_CARD, es una métrica distinta.
 */
class ObserveCreditAvailable @Inject constructor(
    private val accountRepository: AccountRepository,
    private val periodRepository: PeriodRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<Map<String, Long>> =
        combine(accountRepository.observeAll(), transactionRepository.observeAll()) { accounts, transactions ->
            accounts
                .filter { it.type == AccountType.CREDIT_CARD }
                .mapNotNull { account ->
                    val creditLimitCents = account.creditLimitCents ?: return@mapNotNull null
                    val active = periodRepository.getActiveByAccount(account.id).firstOrNull()
                        ?: return@mapNotNull null

                    val spent = transactions
                        .filter { it.periodId == active.id && it.kind == TransactionKind.EXPENSE }
                        .sumOf { it.amountCents }

                    val available = creditLimitCents - (active.carriedBalanceCents + spent - active.amountPaidCents)
                    account.id to available
                }
                .toMap()
        }
}
