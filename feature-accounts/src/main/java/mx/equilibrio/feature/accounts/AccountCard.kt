package mx.equilibrio.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioColors
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeLarge
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.eqShadow
import mx.equilibrio.ui.theme.tabularAmountStyle

val AccountCardHeight = 184.dp

/** (profundo, base, medio) de la familia de dominio que le toca a este tipo de cuenta. */
private fun accountPalette(type: AccountType, colors: EquilibrioColors): Triple<Color, Color, Color> =
    when (type) {
        AccountType.CREDIT_CARD -> Triple(colors.purpleDeep, colors.purple, colors.purpleMid)
        AccountType.CASH, AccountType.BANK -> Triple(colors.greenDeep, colors.green, colors.greenMid)
    }

internal fun accountGradient(type: AccountType, colorSlot: Int, colors: EquilibrioColors): Brush {
    val (deep, base, mid) = accountPalette(type, colors)
    val (from, to) = when (((colorSlot % 3) + 3) % 3) {
        0 -> deep to mid
        1 -> base to deep
        else -> mid to base
    }
    return Brush.linearGradient(listOf(from, to))
}

/** Tarjeta de cuenta tipo billetera: gradiente por tipo/colorSlot, dígitos enmascarados, disponible y vencimiento. */
@Composable
fun AccountCard(account: AccountUi, modifier: Modifier = Modifier) {
    val colors = EquilibrioTheme.colors
    Column(
        modifier = modifier
            .eqShadow(Elevation.LEVEL_2, ShapeLarge)
            .background(accountGradient(account.type, account.colorSlot, colors), ShapeLarge)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = account.name,
                style = EquilibrioTheme.typography.h3,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.maskedDigits,
                style = EquilibrioTheme.typography.label,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "Disponible",
                    style = EquilibrioTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Text(
                    text = formatCents(account.balanceCents),
                    style = tabularAmountStyle(fontSize = 28.sp),
                    color = Color.White,
                )
            }
            if (account.dueDay != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Vence",
                        style = EquilibrioTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Text(
                        text = "Día ${account.dueDay}",
                        style = EquilibrioTheme.typography.label,
                        color = Color.White,
                    )
                }
            }
        }
    }
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
