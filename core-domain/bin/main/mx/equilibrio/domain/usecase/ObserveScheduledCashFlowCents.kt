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
 * Neto de movimientos SCHEDULED en cuentas CASH/BANK (manuales o recurrentes) —
 * lo que le falta a [ObserveAvailableBalances] (que solo mira COMPLETED) para
 * que el saldo proyectado se mueva al programar algo a futuro. Sin excluir
 * `goalId` (abono a meta): mismo criterio que [ObserveAvailableBalances], que
 * tampoco lo excluye — ahí el dinero sale de la cuenta igual, la exclusión de
 * `goalId` es solo para resúmenes tipo reporte, no para el cálculo de saldo.
 */
class ObserveScheduledCashFlowCents @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<Long> = combine(
        accountRepository.observeAll(),
        transactionRepository.observeAll(),
    ) { accounts, transactions ->
        val cashBankIds = accounts.filter { it.type != AccountType.CREDIT_CARD }.map { it.id }.toSet()
        transactions
            .filter { it.status == TransactionStatus.SCHEDULED && it.accountId in cashBankIds }
            .sumOf { if (it.kind == TransactionKind.INCOME) it.amountCents else -it.amountCents }
    }
}
