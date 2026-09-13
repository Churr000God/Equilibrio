package mx.equilibrio.domain

import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.isValidFor
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClassificationTest {

    @Test
    fun `FIXED es valido para INCOME`() {
        assertTrue(Classification.FIXED.isValidFor(TransactionKind.INCOME))
    }

    @Test
    fun `VARIABLE es valido para INCOME`() {
        assertTrue(Classification.VARIABLE.isValidFor(TransactionKind.INCOME))
    }

    @Test
    fun `ESSENTIAL es valido para EXPENSE`() {
        assertTrue(Classification.ESSENTIAL.isValidFor(TransactionKind.EXPENSE))
    }

    @Test
    fun `RECREATIONAL es valido para EXPENSE`() {
        assertTrue(Classification.RECREATIONAL.isValidFor(TransactionKind.EXPENSE))
    }

    @Test
    fun `FIXED no es valido para EXPENSE`() {
        assertFalse(Classification.FIXED.isValidFor(TransactionKind.EXPENSE))
    }

    @Test
    fun `VARIABLE no es valido para EXPENSE`() {
        assertFalse(Classification.VARIABLE.isValidFor(TransactionKind.EXPENSE))
    }

    @Test
    fun `ESSENTIAL no es valido para INCOME`() {
        assertFalse(Classification.ESSENTIAL.isValidFor(TransactionKind.INCOME))
    }

    @Test
    fun `RECREATIONAL no es valido para INCOME`() {
        assertFalse(Classification.RECREATIONAL.isValidFor(TransactionKind.INCOME))
    }
}
