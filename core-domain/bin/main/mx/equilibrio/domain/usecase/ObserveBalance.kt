package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * `actualCents`: líquido real (CASH/BANK), sin tocar deuda. `projectedCents`:
 * líquido + neto de SCHEDULED en CASH/BANK ([ObserveScheduledCashFlowCents])
 * - deuda total de tarjetas ([CreditAvailability.totalToPayCents] de todas
 * las cuentas) — evita el doble conteo que había entre pantallas.
 */
data class BalanceTotals(val actualCents: Long, val projectedCents: Long)

class ObserveBalance @Inject constructor(
    private val observeAvailableBalances: ObserveAvailableBalances,
    private val observeCreditAvailable: ObserveCreditAvailable,
    private val observeScheduledCashFlowCents: ObserveScheduledCashFlowCents,
) {
    operator fun invoke(today: LocalDate): Flow<BalanceTotals> = combine(
        observeAvailableBalances(),
        observeCreditAvailable(today),
        observeScheduledCashFlowCents(),
    ) { ownFundsByAccount, creditByAccount, scheduledCashFlow ->
        val ownFunds = ownFundsByAccount.values.sum()
        val totalToPay = creditByAccount.values.sumOf { it.totalToPayCents }
        BalanceTotals(actualCents = ownFunds, projectedCents = ownFunds + scheduledCashFlow - totalToPay)
    }
}
