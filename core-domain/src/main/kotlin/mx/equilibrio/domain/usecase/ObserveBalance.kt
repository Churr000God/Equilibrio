package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * `actualCents`: líquido real (CASH/BANK), sin tocar deuda. `projectedCents`:
 * líquido menos deuda total de tarjetas ([CreditAvailability.totalToPayCents]
 * de todas las cuentas) — evita el doble conteo que había entre pantallas.
 */
data class BalanceTotals(val actualCents: Long, val projectedCents: Long)

class ObserveBalance @Inject constructor(
    private val observeAvailableBalances: ObserveAvailableBalances,
    private val observeCreditAvailable: ObserveCreditAvailable,
) {
    operator fun invoke(today: LocalDate): Flow<BalanceTotals> = combine(
        observeAvailableBalances(),
        observeCreditAvailable(today),
    ) { ownFundsByAccount, creditByAccount ->
        val ownFunds = ownFundsByAccount.values.sum()
        val totalToPay = creditByAccount.values.sumOf { it.totalToPayCents }
        BalanceTotals(actualCents = ownFunds, projectedCents = ownFunds - totalToPay)
    }
}
