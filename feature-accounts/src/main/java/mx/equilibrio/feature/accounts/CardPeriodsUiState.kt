package mx.equilibrio.feature.accounts

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.PeriodState

data class PeriodUi(
    val id: String,
    val state: PeriodState,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val payAt: LocalDate,
    val carriedBalanceCents: Long,
    val spentCents: Long,
    val amountPaidCents: Long,
) {
    /** Saldo del periodo al día de hoy: arrastre + gastado - pagado. Puede quedar a favor (negativo). */
    val balanceCents: Long get() = carriedBalanceCents + spentCents - amountPaidCents
}

/** Estado de la sección de periodos de tarjeta + el diálogo de pago, independiente de [AccountsUiState]. */
data class CardPeriodsUiState(
    val accountId: String? = null,
    val periods: List<PeriodUi> = emptyList(),
    val payTargetPeriodId: String? = null,
    val paySourceAccountId: String? = null,
    val payAmountInput: String = "",
    val isPaying: Boolean = false,
    val payError: String? = null,
    val paySaved: Boolean = false,
)
