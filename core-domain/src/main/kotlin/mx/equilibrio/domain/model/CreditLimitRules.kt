package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

/**
 * Disponible real (solo cargos ya ocurridos) vs proyectado (incluye cargos
 * programados a futuro, ej. cuotas SCHEDULED) de una tarjeta.
 */
data class CreditAvailability(val realCents: Long, val projectedCents: Long)

private data class PeriodProjection(val period: Period, val projectedBalanceCents: Long)

/**
 * El periodo activo es el primero no-CLOSED de [periods] ordenados por
 * `startAt`; los anteriores ya están saldados y no participan. Los periodos
 * posteriores al activo son ciclos futuros que [GetOrCreatePeriodForDate] ya
 * materializó para alojar cuotas a meses — su `carriedBalanceCents` real
 * todavía no importa (siempre nace en 0), así que el proyectado de cada uno
 * es simplemente la suma de sus cargos, asumiendo la factura previa pagada.
 */
private fun periodProjections(periods: List<Period>, charges: List<Transaction>): List<PeriodProjection> {
    val ordered = periods.sortedBy { it.startAt }
    val activeIndex = ordered.indexOfFirst { it.state != PeriodState.CLOSED }
    if (activeIndex == -1) return emptyList()

    val expenses = charges.filter { it.kind == TransactionKind.EXPENSE }
    fun chargesOf(period: Period) = expenses.filter { it.periodId == period.id }

    val active = ordered[activeIndex]
    val activeProjection = PeriodProjection(
        period = active,
        projectedBalanceCents = active.carriedBalanceCents + chargesOf(active).sumOf { it.amountCents } - active.amountPaidCents,
    )
    val futureProjections = ordered.drop(activeIndex + 1).map { period ->
        PeriodProjection(period = period, projectedBalanceCents = chargesOf(period).sumOf { it.amountCents })
    }
    return listOf(activeProjection) + futureProjections
}

fun computeCreditAvailability(
    creditLimitCents: Long,
    periods: List<Period>,
    charges: List<Transaction>,
    today: LocalDate,
): CreditAvailability {
    val ordered = periods.sortedBy { it.startAt }
    val active = ordered.firstOrNull { it.state != PeriodState.CLOSED }
        ?: return CreditAvailability(realCents = creditLimitCents, projectedCents = creditLimitCents)

    val activeCharges = charges.filter { it.kind == TransactionKind.EXPENSE && it.periodId == active.id }
    val realActive = active.carriedBalanceCents +
        activeCharges.filter { it.occurredAt <= today }.sumOf { it.amountCents } -
        active.amountPaidCents

    val worstProjected = periodProjections(periods, charges).maxOf { it.projectedBalanceCents }

    return CreditAvailability(
        realCents = creditLimitCents - realActive,
        projectedCents = creditLimitCents - worstProjected,
    )
}

/**
 * Simula agregar [newCharges] a [existingCharges] y exige que el disponible
 * proyectado no quede negativo en ningún periodo — activo o futuro. Se llama
 * ANTES de persistir nada: si rechaza, no debe quedar ninguna cuota/transacción
 * guardada.
 */
fun requireWithinCreditLimit(
    creditLimitCents: Long,
    periods: List<Period>,
    existingCharges: List<Transaction>,
    newCharges: List<Transaction>,
    today: LocalDate,
) {
    val allCharges = existingCharges + newCharges
    val projections = periodProjections(periods, allCharges)
    val worst = projections.maxByOrNull { it.projectedBalanceCents } ?: return
    val projectedCents = creditLimitCents - worst.projectedBalanceCents
    require(projectedCents >= 0) {
        val exceededByCents = -projectedCents
        "El periodo que cierra el ${worst.period.endAt} quedaría con saldo proyectado de " +
            "${worst.projectedBalanceCents} centavos, $exceededByCents centavos sobre el límite de $creditLimitCents."
    }
}
