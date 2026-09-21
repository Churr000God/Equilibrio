package mx.equilibrio.feature.accounts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqDomainToggleOption
import mx.equilibrio.ui.components.EqFormDialog
import mx.equilibrio.ui.components.EqInlineValidation
import mx.equilibrio.ui.components.EqKeyValueRowGroup
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

private fun periodStateLabel(state: PeriodState): String = when (state) {
    PeriodState.OPEN -> "Abierto"
    PeriodState.AWAITING_PAYMENT -> "Esperando pago"
    PeriodState.CLOSED -> "Cerrado"
}

/** Lista de periodos de facturación de la tarjeta seleccionada, con acción de pago para los no cerrados. */
@Composable
fun CardPeriodsSection(
    periods: List<PeriodUi>,
    onPayClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (periods.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = "Periodos de facturación",
            style = EquilibrioTheme.typography.h3,
            color = EquilibrioTheme.colors.ink,
            modifier = Modifier.padding(horizontal = Spacing.base),
        )
        Column(
            modifier = Modifier.padding(horizontal = Spacing.base),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            periods.forEach { period ->
                PeriodRow(period = period, onPayClicked = { onPayClicked(period.id) })
            }
        }
    }
}

@Composable
private fun PeriodRow(period: PeriodUi, onPayClicked: () -> Unit) {
    val colors = EquilibrioTheme.colors

    EqCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${period.startAt} – ${period.endAt}",
                    style = EquilibrioTheme.typography.bodyStrong,
                    color = colors.ink,
                )
                Text(
                    text = periodStateLabel(period.state),
                    style = EquilibrioTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Text(
                text = "Pago límite: ${period.payAt}",
                style = EquilibrioTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
            EqKeyValueRowGroup(
                items = listOf(
                    "Gastado" to formatCents(period.spentCents),
                    "Pagado" to formatCents(period.amountPaidCents),
                    "Saldo" to formatCents(period.balanceCents),
                ),
            )
            if (period.state != PeriodState.CLOSED) {
                EqButton(
                    text = "Pagar",
                    onClick = onPayClicked,
                    fullWidth = false,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

/** Diálogo simple para pagar un periodo: elegir cuenta origen CASH/BANK + monto (permite pagar de más). */
@Composable
fun PayCreditDialog(
    state: CardPeriodsUiState,
    sourceAccounts: List<AccountUi>,
    onSourceSelected: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    EqFormDialog(
        title = "Pagar tarjeta",
        onDismiss = onDismiss,
        confirmLabel = "Pagar",
        onConfirm = onConfirm,
        confirmLoading = state.isPaying,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(
                text = "Cuenta de origen",
                style = EquilibrioTheme.typography.bodySmall,
                color = EquilibrioTheme.colors.inkMuted,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                sourceAccounts.forEach { account ->
                    EqDomainToggleOption(
                        label = account.name,
                        tone = DomainTone.NEUTRAL,
                        selected = state.paySourceAccountId == account.id,
                        onClick = { onSourceSelected(account.id) },
                    )
                }
            }

            EqTextField(
                value = state.payAmountInput,
                onValueChange = onAmountChanged,
                label = "Monto a pagar",
                keyboardType = KeyboardType.Decimal,
            )

            state.payError?.let { EqInlineValidation(it) }
        }
    }
}
