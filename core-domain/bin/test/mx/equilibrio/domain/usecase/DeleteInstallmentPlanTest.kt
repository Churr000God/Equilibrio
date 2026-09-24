package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DeleteInstallmentPlanTest {

    private fun installment(id: String, planId: String, index: Int, count: Int) = Transaction(
        id = id,
        userId = "u1",
        accountId = "cc1",
        kind = TransactionKind.EXPENSE,
        classification = null as Classification?,
        amountCents = 100_00,
        occurredAt = LocalDate(2026, 6, 10),
        installmentPlanId = planId,
        installmentIndex = index,
        installmentCount = count,
    )

    @Test
    fun `borra todas las cuotas del plan y deja intactas las de otro plan`() = runTest {
        val repository = FakeTransactionRepository()
        repository.seed(
            installment("plan1-1", "plan1", 1, 3),
            installment("plan1-2", "plan1", 2, 3),
            installment("plan1-3", "plan1", 3, 3),
            installment("plan2-1", "plan2", 1, 2),
        )
        val deleteInstallmentPlan = DeleteInstallmentPlan(repository)

        deleteInstallmentPlan("plan1")

        assertNull(repository.getById("plan1-1"))
        assertNull(repository.getById("plan1-2"))
        assertNull(repository.getById("plan1-3"))
        assertNotNull(repository.getById("plan2-1"))
    }
}
