package mx.equilibrio.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.usecase.ObserveReport
import javax.inject.Inject
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    observeReport: ObserveReport,
) : ViewModel() {

    // Mes de "hoy": tope para no dejar navegar el selector hacia meses futuros que todavía no existen.
    private val currentMonth = YearMonth.of(Clock.System.todayIn(TimeZone.UTC))
    private val selectedMonth = MutableStateFlow(currentMonth)

    val state: StateFlow<ReportsUiState> = selectedMonth
        .flatMapLatest { month ->
            observeReport(month).map { report ->
                ReportsUiState(
                    month = month,
                    isLoading = false,
                    report = report,
                    canGoForward = month < currentMonth,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReportsUiState(month = currentMonth),
        )

    fun onEvent(event: ReportsEvent) {
        when (event) {
            ReportsEvent.PreviousMonth -> selectedMonth.update { it.previous() }
            // No hay reporte para meses que aún no pasaron: si ya se está en currentMonth, el evento no hace nada.
            ReportsEvent.NextMonth -> selectedMonth.update { if (it < currentMonth) it.next() else it }
        }
    }
}
