package mx.equilibrio.app.goals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqAlertBanner
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.theme.Spacing

/** RF09 — banner de GOAL_AT_RISK ya funcional aunque la pantalla de Metas (Persona 2) no exista aún. */
@Composable
fun GoalsPlaceholderScreen(
    modifier: Modifier = Modifier,
    viewModel: GoalsPlaceholderViewModel = hiltViewModel(),
) {
    val alerts by viewModel.pendingAlerts.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        alerts.forEach { alert ->
            EqAlertBanner(
                message = alert.message,
                onDismiss = { viewModel.onAlertDismissed(alert.id) },
                modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.xs),
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EqEmptyState(title = "Próximamente", body = "Las metas llegarán en una próxima versión.")
        }
    }
}
