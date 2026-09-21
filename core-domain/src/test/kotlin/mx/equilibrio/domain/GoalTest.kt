package mx.equilibrio.domain

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Goal
import mx.equilibrio.domain.model.GoalStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoalTest {

    private fun goal(target: Long = 6000_00, saved: Long = 0, status: GoalStatus = GoalStatus.ACTIVE) = Goal(
        id = "g1",
        userId = "u1",
        name = "Viaje a la playa",
        targetCents = target,
        savedCents = saved,
        deadline = LocalDate(2026, 12, 31),
        status = status,
        createdAt = LocalDate(2026, 9, 1),
    )

    @Test
    fun `progreso es saved entre target`() {
        assertEquals(0.56f, goal(saved = 3360_00).progress, 0.001f)
    }

    @Test
    fun `progreso se limita a 1 cuando saved supera target`() {
        assertEquals(1f, goal(saved = 9000_00).progress)
        assertEquals(0L, goal(saved = 9000_00).remainingCents)
    }

    @Test
    fun `restante es target menos saved`() {
        assertEquals(2640_00L, goal(saved = 3360_00).remainingCents)
    }

    @Test
    fun `completada por status o por monto`() {
        assertTrue(goal(status = GoalStatus.COMPLETED).isCompleted)
        assertTrue(goal(saved = 6000_00).isCompleted)
        assertFalse(goal(saved = 10_00).isCompleted)
    }

    @Test
    fun `nombre vacio lanza`() {
        assertThrows(IllegalArgumentException::class.java) { goal().copy(name = "  ") }
    }

    @Test
    fun `target no positivo lanza`() {
        assertThrows(IllegalArgumentException::class.java) { goal(target = 0) }
    }

    @Test
    fun `saved negativo lanza`() {
        assertThrows(IllegalArgumentException::class.java) { goal(saved = -1) }
    }
}
