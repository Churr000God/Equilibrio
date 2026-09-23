package mx.equilibrio.domain.usecase

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.computeCreditAvailability
import mx.equilibrio.domain.model.requireWithinCreditLimit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CreditLimitRulesTest {

    private fun period(
        id: String,
        startAt: LocalDate,
        endAt: LocalDate,
        state: PeriodState = PeriodState.OPEN,
        carriedBalanceCents: Long = 0,
        amountPaidCents: Long = 0,
    ) = Period(
        id = id,
        accountId = "cc1",
        startAt = startAt,
        endAt = endAt,
        payAt = endAt,
        state = state,
        carriedBalanceCents = carriedBalanceCents,
        amountPaidCents = amountPaidCents,
    )

    private fun charge(id: String, periodId: String, amountCents: Long, occurredAt: LocalDate) = Transaction(
        id = id,
        userId = "u1",
        accountId = "cc1",
        kind = TransactionKind.EXPENSE,
        classification = null as Classification?,
        amountCents = amountCents,
        occurredAt = occurredAt,
        periodId = periodId,
    )

    @Test
    fun `6 cuotas de 500 en 6 periodos distintos proyecta 2500, no 0`() {
        val today = LocalDate(2026, 6, 15)
        val periods = (0 until 6).map { i -> period(id = "p$i", startAt = LocalDate(2026, i + 6, 1), endAt = LocalDate(2026, i + 6, 28)) }
        val charges = periods.mapIndexed { i, p -> charge("c$i", p.id, 500_00, LocalDate(2026, i + 6, 10)) }

        val availability = computeCreditAvailability(3000_00, periods, charges, today)

        assertEquals(2500_00L, availability.projectedCents)
    }

    @Test
    fun `proyectado negativo cuando una factura se pasa`() {
        val today = LocalDate(2026, 6, 15)
        val p = period(id = "p0", startAt = LocalDate(2026, 6, 1), endAt = LocalDate(2026, 6, 28))
        val charges = listOf(
            charge("c1", p.id, 700_00, LocalDate(2026, 6, 10)),
            charge("c2", p.id, 500_00, LocalDate(2026, 6, 20)),
        )

        val availability = computeCreditAvailability(1000_00, listOf(p), charges, today)

        assertEquals(-200_00L, availability.projectedCents)
    }

    @Test
    fun `real distinto de proyectado cuando hay cuotas futuras en el periodo activo`() {
        val today = LocalDate(2026, 6, 15)
        val p = period(id = "p0", startAt = LocalDate(2026, 6, 1), endAt = LocalDate(2026, 6, 28))
        val charges = listOf(
            charge("c1", p.id, 300_00, LocalDate(2026, 6, 10)),
            charge("c2", p.id, 400_00, LocalDate(2026, 6, 25)),
        )

        val availability = computeCreditAvailability(1000_00, listOf(p), charges, today)

        assertEquals(700_00L, availability.realCents)
        assertEquals(300_00L, availability.projectedCents)
    }

    @Test
    fun `requireWithinCreditLimit rechaza cuando el proyectado queda negativo`() {
        val today = LocalDate(2026, 6, 15)
        val p = period(id = "p0", startAt = LocalDate(2026, 6, 1), endAt = LocalDate(2026, 6, 28))
        val existing = listOf(charge("c1", p.id, 700_00, LocalDate(2026, 6, 10)))
        val new = listOf(charge("c2", p.id, 400_00, LocalDate(2026, 6, 20)))

        assertThrows(IllegalArgumentException::class.java) {
            requireWithinCreditLimit(1000_00, listOf(p), existing, new, today)
        }
    }

    @Test
    fun `requireWithinCreditLimit acepta justo en el limite`() {
        val today = LocalDate(2026, 6, 15)
        val p = period(id = "p0", startAt = LocalDate(2026, 6, 1), endAt = LocalDate(2026, 6, 28))
        val new = listOf(charge("c1", p.id, 1000_00, LocalDate(2026, 6, 10)))

        requireWithinCreditLimit(1000_00, listOf(p), emptyList(), new, today)
    }

    @Test
    fun `carryover del periodo activo cuenta, el de un periodo futuro no`() {
        val today = LocalDate(2026, 6, 15)
        val active = period(
            id = "p0",
            startAt = LocalDate(2026, 6, 1),
            endAt = LocalDate(2026, 6, 28),
            carriedBalanceCents = 400_00,
        )
        val future = period(
            id = "p1",
            startAt = LocalDate(2026, 7, 1),
            endAt = LocalDate(2026, 7, 28),
            carriedBalanceCents = 900_00,
        )
        val charges = listOf(
            charge("c1", active.id, 100_00, LocalDate(2026, 6, 10)),
            charge("c2", future.id, 100_00, LocalDate(2026, 7, 10)),
        )

        val availability = computeCreditAvailability(1000_00, listOf(active, future), charges, today)

        assertEquals(500_00L, availability.projectedCents)
    }
}
