package mx.equilibrio.feature.home

import mx.equilibrio.domain.model.report.ReportSection
import mx.equilibrio.ui.components.EqAccountFamily
import mx.equilibrio.ui.theme.DomainTone

/** Tarjeta del carrusel de Inicio: tipo, dígitos enmascarados y disponible. */
data class HomeCardUi(
    val id: String,
    val typeLabel: String,
    val maskedDigits: String,
    val availableCents: Long,
    val family: EqAccountFamily,
    val colorSlot: Int,
    val dueDay: Int?,
)

/** Acceso directo a una sección de Reportes, con una lectura corta del mes. */
data class ReportHighlightUi(
    val section: ReportSection,
    val title: String,
    val description: String,
    val tone: DomainTone,
)

data class HomeDashboardUiState(
    val isLoading: Boolean = true,
    val greetingName: String? = null,
    val cards: List<HomeCardUi> = emptyList(),
    /** Suma del disponible en efectivo y banco; el crédito no cuenta como saldo. */
    val balanceCents: Long = 0,
    /** `balanceCents` menos la deuda total de tarjetas (incluye cuotas futuras). */
    val projectedBalanceCents: Long = 0,
    val recentTransactions: List<TransactionUi> = emptyList(),
    val reportHighlights: List<ReportHighlightUi> = emptyList(),
)
