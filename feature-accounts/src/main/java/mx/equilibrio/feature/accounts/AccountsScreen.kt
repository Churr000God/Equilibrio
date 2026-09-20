package mx.equilibrio.feature.accounts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqAlertBanner
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqFab
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

@Composable
fun AccountsScreen(
    onAddAccountClicked: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = { EqTopBar(title = "Cuentas", trailing = trailing) },
        floatingActionButton = { EqFab(onClick = onAddAccountClicked) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            state.pendingAlerts.forEach { alert ->
                EqAlertBanner(
                    message = alert.message,
                    onDismiss = { viewModel.onAlertDismissed(alert.id) },
                    modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.xs),
                )
            }

            when {
                state.isEmpty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EqEmptyState(
                        title = "Aún no tienes cuentas",
                        body = "Agrega tu primera cuenta para empezar a registrar movimientos.",
                    )
                }

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    AccountsCarousel(
                        accounts = state.accounts,
                        selectedAccountId = state.selectedAccountId,
                        onAccountSelected = viewModel::onAccountSelected,
                        modifier = Modifier.padding(top = Spacing.base),
                    )
                    // El detalle de la cuenta seleccionada (state.selectedAccountId) se conecta en otra tarea.
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsScreenEmptyPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            EqEmptyState(
                title = "Aún no tienes cuentas",
                body = "Agrega tu primera cuenta para empezar a registrar movimientos.",
            )
        }
    }
}
