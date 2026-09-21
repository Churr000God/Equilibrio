package mx.equilibrio.app.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType
import mx.equilibrio.domain.usecase.GetPendingAlertsUseCase
import mx.equilibrio.domain.usecase.MarkAlertAsReadUseCase
import javax.inject.Inject

data class GoalAlertUi(
    val id: String,
    val message: String,
)

private fun Alert.toUi() = GoalAlertUi(id = id, message = "Una de tus metas está en riesgo.")

/** Alimenta el banner de RF09 en la ruta de Metas mientras :feature-goals no existe (Persona 2). */
@HiltViewModel
class GoalsPlaceholderViewModel @Inject constructor(
    getPendingAlerts: GetPendingAlertsUseCase,
    private val markAlertAsRead: MarkAlertAsReadUseCase,
) : ViewModel() {

    val pendingAlerts: StateFlow<List<GoalAlertUi>> = getPendingAlerts()
        .map { alerts -> alerts.filter { it.type == AlertType.GOAL_AT_RISK }.map { it.toUi() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun onAlertDismissed(alertId: String) {
        viewModelScope.launch { markAlertAsRead(alertId) }
    }
}
