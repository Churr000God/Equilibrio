package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import javax.inject.Inject

/** Cuenta con el disponible que le corresponde según su tipo. */
data class AccountAvailable(
    val account: Account,
    val availableCents: Long,
)

/** Suma del disponible en efectivo y banco; el crédito es dinero prestado y no cuenta como saldo. */
fun List<AccountAvailable>.ownFundsCents(): Long =
    filter { it.account.type == AccountType.CASH || it.account.type == AccountType.BANK }
        .sumOf { it.availableCents }

/**
 * Une [ObserveAvailableBalances] (CASH/BANK) y [ObserveCreditAvailable] (CREDIT_CARD)
 * en una sola lista. Si una cuenta aún no tiene cálculo (p. ej. tarjeta sin periodo
 * activo) cae a su `balanceCents` guardado.
 */
class ObserveAccountsAvailable @Inject constructor(
    private val observeAccounts: ObserveAccounts,
    private val observeAvailableBalances: ObserveAvailableBalances,
    private val observeCreditAvailable: ObserveCreditAvailable,
) {
    operator fun invoke(): Flow<List<AccountAvailable>> = combine(
        observeAccounts(),
        observeAvailableBalances(),
        observeCreditAvailable(),
    ) { accounts, availableBalances, creditAvailable ->
        accounts.map { account ->
            val available = when (account.type) {
                AccountType.CASH, AccountType.BANK -> availableBalances[account.id]
                AccountType.CREDIT_CARD -> creditAvailable[account.id]
            }
            AccountAvailable(account, available ?: account.balanceCents)
        }
    }
}
