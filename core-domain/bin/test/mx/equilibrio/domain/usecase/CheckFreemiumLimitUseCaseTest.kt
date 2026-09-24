package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.FreemiumResult
import mx.equilibrio.domain.model.PlanTier
import mx.equilibrio.domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CheckFreemiumLimitUseCaseTest {

    private fun account(id: String, type: AccountType) =
        Account(id = id, userId = "u1", name = id, type = type)

    private fun user(plan: PlanTier) = User(
        id = "u1", googleId = null, email = null, displayName = null,
        givenName = null, familyName = null, photoUrl = null, plan = plan,
    )

    private fun useCase(plan: PlanTier, vararg accounts: Account) = CheckFreemiumLimitUseCase(
        GetAccountCountUseCase(FakeAccountRepository(accounts.associateBy { it.id })),
        FakeUserRepository(currentUser = user(plan)),
    )

    @Test
    fun `FREE con Efectivo y 1 banco puede crear tarjeta de crédito`() = runTest {
        val check = useCase(PlanTier.FREE, account("cash", AccountType.CASH), account("bank", AccountType.BANK))

        assertEquals(FreemiumResult.OK, check(AccountType.CREDIT_CARD))
    }

    @Test
    fun `FREE con Efectivo y 2 cuentas no-CASH alcanza el límite`() = runTest {
        val check = useCase(
            PlanTier.FREE,
            account("cash", AccountType.CASH),
            account("bank", AccountType.BANK),
            account("cc", AccountType.CREDIT_CARD),
        )

        assertEquals(FreemiumResult.LIMIT_REACHED, check(AccountType.BANK))
    }

    @Test
    fun `FREE en el límite aún puede crear cuentas de Efectivo`() = runTest {
        val check = useCase(PlanTier.FREE, account("bank", AccountType.BANK), account("cc", AccountType.CREDIT_CARD))

        assertEquals(FreemiumResult.OK, check(AccountType.CASH))
    }

    @Test
    fun `PREMIUM no tiene límite`() = runTest {
        val accounts = (1..5).map { account("bank$it", AccountType.BANK) }.toTypedArray()
        val check = useCase(PlanTier.PREMIUM, *accounts)

        assertEquals(FreemiumResult.OK, check(AccountType.CREDIT_CARD))
    }
}
