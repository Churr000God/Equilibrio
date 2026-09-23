package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import javax.inject.Inject

/** Cuenta con el disponible que le corresponde según su tipo. */
data class AccountAvailable(
    val account: Account,
    val availableCents: Long,
)

/**
 * Une [ObserveAvailableBalances] (CASH/BANK) y [ObserveCreditAvailable] (CREDIT_CARD,
 * usando el disponible real — [today] resuelve qué cargos ya "ocurrieron") en una sola
 * lista. Si una cuenta aún no tiene cálculo cae a su `balanceCents` guardado.
 */
class ObserveAccountsAvailable @Inject constructor(
    private val observeAccounts: ObserveAccounts,
    private val observeAvailableBalances: ObserveAvailableBalances,
    private val observeCreditAvailable: ObserveCreditAvailable,
) {
    operator fun invoke(today: LocalDate): Flow<List<AccountAvailable>> = combine(
        observeAccounts(),
        observeAvailableBalances(),
        observeCreditAvailable(today),
    ) { accounts, availableBalances, creditAvailable ->
        accounts.map { account ->
            val available = when (account.type) {
                AccountType.CASH, AccountType.BANK -> availableBalances[account.id]
                AccountType.CREDIT_CARD -> creditAvailable[account.id]?.realCents
            }
            AccountAvailable(account, available ?: account.balanceCents)
        }
    }
}
