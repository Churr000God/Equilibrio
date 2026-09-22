package mx.equilibrio.feature.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.ui.components.EqAccountCard
import mx.equilibrio.ui.components.EqAccountCardHeight
import mx.equilibrio.ui.components.EqAccountFamily
import mx.equilibrio.ui.components.eqAccountSlotColor
import mx.equilibrio.ui.theme.EquilibrioColors
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

val AccountCardHeight = EqAccountCardHeight

/** Familia de dominio que le toca a este tipo de cuenta. */
internal fun AccountType.family(): EqAccountFamily = when (this) {
    AccountType.CREDIT_CARD -> EqAccountFamily.PURPLE
    AccountType.CASH, AccountType.BANK -> EqAccountFamily.GREEN
}

/** Color sólido representativo de un slot, para el picker. */
internal fun accountSlotColor(type: AccountType, colorSlot: Int, colors: EquilibrioColors): Color =
    eqAccountSlotColor(type.family(), colorSlot, colors)

/** Tarjeta de cuenta tipo billetera; el render vive en [EqAccountCard]. */
@Composable
fun AccountCard(account: AccountUi, modifier: Modifier = Modifier) {
    EqAccountCard(
        title = account.name,
        maskedDigits = account.maskedDigits,
        availableCents = account.balanceCents,
        family = account.type.family(),
        colorSlot = account.colorSlot,
        dueDay = account.dueDay,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun AccountCardPreview() {
    EquilibrioTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.base),
            verticalArrangement = Arrangement.spacedBy(Spacing.base),
        ) {
            AccountCard(
                account = AccountUi(
                    id = "1",
                    name = "Cuenta débito",
                    type = AccountType.BANK,
                    balanceCents = 1_254_300,
                    colorSlot = 0,
                    lastDigits = 4821,
                ),
                modifier = Modifier.width(320.dp).height(AccountCardHeight),
            )
            AccountCard(
                account = AccountUi(
                    id = "2",
                    name = "Tarjeta oro",
                    type = AccountType.CREDIT_CARD,
                    balanceCents = 850_000,
                    colorSlot = 1,
                    lastDigits = null,
                    dueDay = 12,
                ),
                modifier = Modifier.width(320.dp).height(AccountCardHeight),
            )
        }
    }
}
