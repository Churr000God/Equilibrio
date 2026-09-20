package mx.equilibrio.domain

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CategoryTest {

    @Test
    fun `fromKey resuelve claves conocidas`() {
        assertEquals(Category.FOOD, Category.fromKey("food"))
        assertEquals(Category.SAVINGS, Category.fromKey("savings"))
    }

    @Test
    fun `fromKey devuelve null para null o desconocida`() {
        assertNull(Category.fromKey(null))
        assertNull(Category.fromKey("pets"))
    }

    @Test
    fun `selectable excluye SAVINGS`() {
        assertFalse(Category.SAVINGS in Category.selectable)
        assertEquals(Category.entries.size - 1, Category.selectable.size)
    }

    @Test
    fun `SAVINGS en un ingreso lanza`() {
        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = "t", userId = "u", accountId = "a",
                kind = TransactionKind.INCOME, classification = null,
                amountCents = 100, occurredAt = LocalDate(2026, 9, 1),
                category = Category.SAVINGS,
            )
        }
    }

    @Test
    fun `SAVINGS en un gasto sin clasificar es abono`() {
        val t = Transaction(
            id = "t", userId = "u", accountId = "a",
            kind = TransactionKind.EXPENSE, classification = null,
            amountCents = 100, occurredAt = LocalDate(2026, 9, 1),
            category = Category.SAVINGS,
        )
        assertTrue(t.isSavings)
    }
}
