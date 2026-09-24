package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

/**
 * Ciclo de corte/pago de una cuenta CREDIT_CARD (ver [deriveCycleBounds]).
 * `carriedBalanceCents` es la deuda con la que abre el periodo; `amountPaidCents`,
 * lo ya pagado contra ese saldo. Ambos alimentan el disponible real/proyectado
 * en `CreditLimitRules`.
 */
data class Period(
    val id: String,
    val accountId: String,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val payAt: LocalDate,
    val state: PeriodState,
    val carriedBalanceCents: Long,
    val amountPaidCents: Long,
)
