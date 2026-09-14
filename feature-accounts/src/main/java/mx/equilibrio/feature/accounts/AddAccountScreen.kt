package mx.equilibrio.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = { EqTopBar(title = "Nueva cuenta", onBack = onCancel) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = Spacing.base)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            EqSegmentedControl(
                options = TYPE_OPTIONS.map { it.first },
                selectedIndex = TYPE_OPTIONS.indexOfFirst { it.second == state.type },
                onSelect = { index -> viewModel.onEvent(AddAccountEvent.TypeChanged(TYPE_OPTIONS[index].second)) },
            )

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

            AccountColorPicker(
                type = state.type,
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

@Composable
private fun AccountColorPicker(
    type: AccountType,
    selectedSlot: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = EquilibrioTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = "Color", style = EquilibrioTheme.typography.bodySmall, color = colors.inkMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            repeat(AccountColorSlotCount) { slot ->
                val selected = slot == selectedSlot
                Row(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onSelect(slot) }
                        .background(accountGradient(type, slot, colors), ShapePill)
                        .border(if (selected) 2.dp else 0.dp, colors.ink, ShapePill),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selected) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountColorPickerPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            AccountColorPicker(type = AccountType.CREDIT_CARD, selectedSlot = 1, onSelect = {})
        }
    }
}
