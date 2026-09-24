package mx.equilibrio.feature.accounts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.ui.components.EqAlertBanner
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqFab
import mx.equilibrio.ui.components.EqSkeleton
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
    val cardPeriodsState by viewModel.cardPeriodsState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(title = "Cuentas", trailing = trailing)

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.pendingAlerts.forEach { alert ->
                    EqAlertBanner(
                        message = alert.message,
                        onDismiss = { viewModel.onAlertDismissed(alert.id) },
                        modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.xs),
                    )
                }

                when {
                    state.isLoading -> EqSkeleton(rows = 1, rowHeight = AccountCardHeight)

                    state.isEmpty -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        EqEmptyState(
                            title = "Aún no tienes cuentas",
                            body = "Agrega tu primera cuenta para empezar a registrar movimientos.",
                        )
                    }

                    else -> Column(
                        // clipToBounds(): el stretch de overscroll no debe pintar fuera de sus bounds.
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        AccountsCarousel(
                            accounts = state.accounts,
                            selectedAccountId = state.selectedAccountId,
                            onAccountSelected = viewModel::onAccountSelected,
                            modifier = Modifier.padding(top = Spacing.base),
                        )

                        val selectedAccount = state.accounts.firstOrNull { it.id == state.selectedAccountId }
                        // Periodos de facturación solo aplican a tarjetas de crédito; débito/efectivo no tienen corte.
                        if (selectedAccount?.type == AccountType.CREDIT_CARD) {
                            CardPeriodsSection(
                                periods = cardPeriodsState.periods,
                                onPayClicked = viewModel::onPayClicked,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Spacing.lg),
                            )
                        }
                    }
                }

                if (cardPeriodsState.payTargetPeriodId != null) {
                    PayCreditDialog(
                        state = cardPeriodsState,
                        sourceAccounts = state.accounts.filter {
                            it.type == AccountType.CASH || it.type == AccountType.BANK
                        },
                        onSourceSelected = viewModel::onPaySourceSelected,
                        onAmountChanged = viewModel::onPayAmountChanged,
                        onConfirm = viewModel::onPayConfirmed,
                        onDismiss = viewModel::onPayDismissed,
                    )
                }
            }

            EqFab(onClick = onAddAccountClicked, modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg))
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
