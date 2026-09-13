package mx.equilibrio.domain

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TransactionTest {

    private val today = LocalDate(2026, 9, 13)

    @Test
    fun `construir con clasificacion valida no lanza`() {
        Transaction(
            id = "t1",
            userId = "u1",
            accountId = "a1",
            kind = TransactionKind.INCOME,
            classification = Classification.FIXED,
            amountCents = 500_00,
            occurredAt = today,
        )
    }

    @Test
    fun `construir con classification null no lanza`() {
        Transaction(
            id = "t1",
            userId = "u1",
            accountId = "a1",
            kind = TransactionKind.EXPENSE,
            classification = null,
            amountCents = 100,
            occurredAt = today,
        )
    }

    @Test
    fun `construir con par invalido lanza`() {
        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = "t1",
                userId = "u1",
                accountId = "a1",
                kind = TransactionKind.INCOME,
                classification = Classification.ESSENTIAL,
                amountCents = 100,
                occurredAt = today,
            )
        }
    }

    @Test
    fun `construir con monto cero lanza`() {
        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = "t1",
                userId = "u1",
                accountId = "a1",
                kind = TransactionKind.EXPENSE,
                classification = Classification.ESSENTIAL,
                amountCents = 0,
                occurredAt = today,
            )
        }
    }

    @Test
    fun `construir con monto negativo lanza`() {
        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = "t1",
                userId = "u1",
                accountId = "a1",
                kind = TransactionKind.EXPENSE,
                classification = Classification.ESSENTIAL,
                amountCents = -100,
                occurredAt = today,
            )
        }
    }
}
