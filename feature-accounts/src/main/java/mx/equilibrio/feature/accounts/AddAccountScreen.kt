package mx.equilibrio.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.ui.components.EqAlertBanner
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqColorSlotPicker
import mx.equilibrio.ui.components.EqInfoDialog
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

private val TYPE_OPTIONS = listOf("Efectivo" to AccountType.CASH, "Débito" to AccountType.BANK, "Crédito" to AccountType.CREDIT_CARD)

@Composable
fun AddAccountScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddAccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    if (state.freemiumLimitReached) {
        EqInfoDialog(
            title = "Límite del plan gratuito",
            body = "El plan FREE permite hasta 2 cuentas de banco o tarjeta (Efectivo no cuenta). Actualiza a Premium para agregar más.",
            confirmLabel = "Entendido",
            onConfirm = { viewModel.onEvent(AddAccountEvent.FreemiumDialogDismissed) },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(title = "Nueva cuenta", onBack = onCancel)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.base)
                .clipToBounds()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            EqSegmentedControl(
                options = TYPE_OPTIONS.map { it.first },
                selectedIndex = TYPE_OPTIONS.indexOfFirst { it.second == state.type },
                onSelect = { index -> viewModel.onEvent(AddAccountEvent.TypeChanged(TYPE_OPTIONS[index].second)) },
            )

            if (state.blockedByFreemium) {
                EqAlertBanner(
                    message = "Llegaste al límite de 2 cuentas de banco o tarjeta del plan FREE. Puedes agregar cuentas de Efectivo.",
                    onDismiss = { viewModel.onEvent(AddAccountEvent.TypeChanged(AccountType.CASH)) },
                )
            }

            EqTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(AddAccountEvent.NameChanged(it)) },
                label = "Nombre de la cuenta",
                isError = state.nameError != null,
                helperOrError = state.nameError,
            )

            EqTextField(
                value = state.balanceInput,
                onValueChange = { viewModel.onEvent(AddAccountEvent.BalanceChanged(it)) },
                label = if (state.type == AccountType.CREDIT_CARD) "Deuda actual" else "Saldo actual",
                keyboardType = KeyboardType.Decimal,
                isError = state.balanceError != null,
                helperOrError = state.balanceError,
            )

            if (state.type == AccountType.CREDIT_CARD) {
                EqTextField(
                    value = state.creditLimitInput,
                    onValueChange = { viewModel.onEvent(AddAccountEvent.CreditLimitChanged(it)) },
                    label = "Límite de crédito",
                    keyboardType = KeyboardType.Decimal,
                    isError = state.creditLimitError != null,
                    helperOrError = state.creditLimitError,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    EqTextField(
                        value = state.statementDayInput,
                        onValueChange = { viewModel.onEvent(AddAccountEvent.StatementDayChanged(it)) },
                        label = "Día de corte",
                        keyboardType = KeyboardType.Number,
                        isError = state.statementDayError != null,
                        helperOrError = state.statementDayError,
                        modifier = Modifier.weight(1f),
                    )
                    EqTextField(
                        value = state.dueDayInput,
                        onValueChange = { viewModel.onEvent(AddAccountEvent.DueDayChanged(it)) },
                        label = "Día de pago",
                        keyboardType = KeyboardType.Number,
                        isError = state.dueDayError != null,
                        helperOrError = state.dueDayError,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (state.type != AccountType.CASH) {
                EqTextField(
                    value = state.lastDigitsInput,
                    onValueChange = { viewModel.onEvent(AddAccountEvent.LastDigitsChanged(it)) },
                    label = "Últimos 4 dígitos (opcional)",
                    keyboardType = KeyboardType.Number,
                    isError = state.lastDigitsError != null,
                    helperOrError = state.lastDigitsError,
                )
            }

            val themeColors = EquilibrioTheme.colors
            EqColorSlotPicker(
                colors = (0 until AccountColorSlotCount).map { slot -> accountSlotColor(state.type, slot, themeColors) },
                selectedSlot = state.colorSlot,
                onSelect = { viewModel.onEvent(AddAccountEvent.ColorSlotChanged(it)) },
            )

            EqButton(
                text = "Guardar cuenta",
                onClick = { viewModel.onEvent(AddAccountEvent.SaveClicked) },
                enabled = state.canSave,
                loading = state.isSaving,
                modifier = Modifier.padding(vertical = Spacing.base),
            )
        }
    }
}

