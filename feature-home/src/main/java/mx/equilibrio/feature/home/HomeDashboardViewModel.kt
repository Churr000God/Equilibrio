package mx.equilibrio.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.model.report.Report
import mx.equilibrio.domain.model.report.ReportSection
import mx.equilibrio.domain.usecase.AccountAvailable
import mx.equilibrio.domain.usecase.GenerateDueRecurringTransactions
import mx.equilibrio.domain.usecase.ObserveAccountsAvailable
import mx.equilibrio.domain.usecase.ObserveBalance
import mx.equilibrio.domain.usecase.ObserveCurrentUser
import mx.equilibrio.domain.usecase.ObserveReport
import mx.equilibrio.domain.usecase.ObserveTransactions
import mx.equilibrio.ui.components.EqAccountFamily
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Clock

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    observeAccountsAvailable: ObserveAccountsAvailable,
    observeBalance: ObserveBalance,
    observeTransactions: ObserveTransactions,
    observeReport: ObserveReport,
    private val generateDueRecurringTransactions: GenerateDueRecurringTransactions,
) : ViewModel() {

    /** Orden y cantidad de reportes se sortean una vez por carga de la pantalla. */
    private val reportOrder = ReportSection.entries.shuffled()
    private val reportCount = Random.nextInt(MIN_REPORTS, MAX_REPORTS + 1)

    private val today = Clock.System.todayIn(TimeZone.UTC)

    init {
        viewModelScope.launch { generateDueRecurringTransactions(today) }
    }

    val state: StateFlow<HomeDashboardUiState> = combine(
        observeCurrentUser(),
        observeAccountsAvailable(today),
        observeTransactions(),
        observeReport(YearMonth.of(today)),
        observeBalance(today),
    ) { user, accounts, transactions, report, balance ->
        HomeDashboardUiState(
            isLoading = false,
            // RF01 — el saludo usa el nombre de usuario elegido en Perfil, no el nombre de Google.
            greetingName = user?.displayName?.takeIf { it.isNotBlank() },
            cards = accounts.map { it.toCardUi() },
            balanceCents = balance.actualCents,
            projectedBalanceCents = balance.projectedCents,
            // El repositorio ya entrega los movimientos del más reciente al más antiguo.
            recentTransactions = transactions.take(RECENT_TRANSACTIONS).map { it.toUi() },
            reportHighlights = if (report.isEmpty) emptyList() else highlightsOf(report),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeDashboardUiState(isLoading = true),
    )

    private fun highlightsOf(report: Report): List<ReportHighlightUi> =
        reportOrder
            .filter { it != ReportSection.CATEGORIES || report.categories.isNotEmpty() }
            .take(reportCount)
            .map { it.toHighlight(report) }

    private fun ReportSection.toHighlight(report: Report): ReportHighlightUi = when (this) {
        ReportSection.SUMMARY -> {
            val leftover = report.comparison.current.leftoverCents
            ReportHighlightUi(
                section = this,
                title = "Tu mes",
                description = if (leftover >= 0) {
                    "Te quedaron ${formatCents(leftover)} este mes."
                } else {
                    "Este mes gastaste ${formatCents(-leftover)} más de lo que entró."
                },
                tone = DomainTone.ESSENTIAL,
            )
        }

        ReportSection.TREND -> ReportHighlightUi(
            section = this,
            title = "Últimos 6 meses",
            description = "Tus ingresos contra tus gastos, mes a mes.",
            tone = DomainTone.INCOME_VARIABLE,
        )

        ReportSection.MATRIX -> ReportHighlightUi(
            section = this,
            title = "De dónde sale cada gasto",
            description = "Tu ingreso fijo y variable cruzado con lo esencial y lo recreativo.",
            tone = DomainTone.RECREATIONAL,
        )

        ReportSection.CATEGORIES -> {
            val top = report.categories.first()
            ReportHighlightUi(
                section = this,
                title = "En qué se te va más",
                description = "${top.name ?: "Sin categoría"}: ${formatCents(top.cents)} este mes.",
                tone = DomainTone.INCOME_FIXED,
            )
        }
    }

    private fun AccountAvailable.toCardUi() = HomeCardUi(
        id = account.id,
        typeLabel = when (account.type) {
            AccountType.CASH -> "Efectivo"
            AccountType.BANK -> "Banco"
            AccountType.CREDIT_CARD -> "Crédito"
        },
        maskedDigits = account.lastDigits?.let { "•••• " + it.toString().padStart(4, '0').takeLast(4) } ?: "****",
        availableCents = availableCents,
        family = when (account.type) {
            AccountType.CREDIT_CARD -> EqAccountFamily.PURPLE
            AccountType.CASH, AccountType.BANK -> EqAccountFamily.GREEN
        },
        colorSlot = account.colorSlot,
        dueDay = account.dueDay,
    )

    private companion object {
        const val RECENT_TRANSACTIONS = 2
        const val MIN_REPORTS = 2
        const val MAX_REPORTS = 3
    }
}
