package mx.equilibrio.feature.accounts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.distinctUntilChanged
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import kotlin.math.abs

/**
 * Carrusel de tarjetas con peek (se asoma el borde de la siguiente) y snap.
 * Reporta la cuenta activa vía [onAccountSelected] — el hook para la sección
 * de detalle que depende de la cuenta seleccionada; esa sección no vive acá.
 */
@Composable
fun AccountsCarousel(
    accounts: List<AccountUi>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (accounts.isEmpty()) return

    val initialPage = remember(accounts) {
        accounts.indexOfFirst { it.id == selectedAccountId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { accounts.size }

    LaunchedEffect(pagerState, accounts) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page -> accounts.getOrNull(page)?.let { onAccountSelected(it.id) } }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.xxl),
        pageSpacing = Spacing.base,
    ) { page ->
        val distanceFromCurrent = abs(
            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction,
        ).coerceIn(0f, 1f)
        val settle = 1f - distanceFromCurrent

        AccountCard(
            account = accounts[page],
            modifier = Modifier
                .fillMaxWidth()
                .height(AccountCardHeight)
                .graphicsLayer {
                    val scale = lerp(0.94f, 1f, settle)
                    scaleX = scale
                    scaleY = scale
                }
                .alpha(lerp(0.6f, 1f, settle)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsCarouselPreview() {
    val accounts = listOf(
        AccountUi(id = "1", name = "Cuenta débito", type = AccountType.BANK, balanceCents = 1_254_300, colorSlot = 0, lastDigits = 4821),
        AccountUi(id = "2", name = "Tarjeta oro", type = AccountType.CREDIT_CARD, balanceCents = 850_000, colorSlot = 1, dueDay = 12),
        AccountUi(id = "3", name = "Efectivo", type = AccountType.CASH, balanceCents = 32_000, colorSlot = 2),
    )
    EquilibrioTheme {
        AccountsCarousel(
            accounts = accounts,
            selectedAccountId = accounts.first().id,
            onAccountSelected = {},
        )
    }
}
