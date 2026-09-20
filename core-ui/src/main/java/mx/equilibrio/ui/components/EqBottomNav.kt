package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget
import mx.equilibrio.ui.theme.eqShadow

/** Los cinco destinos del producto menos Registro (que es un FAB). */
enum class EqNavDestination(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("Inicio", Icons.Outlined.Home, Icons.Rounded.Home),
    ACCOUNTS("Cuentas", Icons.Outlined.AccountBalanceWallet, Icons.Rounded.AccountBalanceWallet),
    GOALS("Metas", Icons.Outlined.Flag, Icons.Rounded.Flag),
    REPORTS("Reportes", Icons.Outlined.PieChart, Icons.Rounded.PieChart),
}

/**
 * Barra capsular flotante (§07). Ítem activo: píldora `greenSoft`, icono
 * relleno y texto `greenDeep`. Sin colores de estado.
 */
@Composable
fun EqBottomNav(
    current: EqNavDestination,
    onSelect: (EqNavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = modifier
            .padding(horizontal = Spacing.base)
            .eqShadow(Elevation.LEVEL_2, ShapePill)
            .background(colors.surface, ShapePill)
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        EqNavDestination.entries.forEach { destination ->
            val selected = destination == current
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = TouchTarget.minSize)
                    .background(if (selected) colors.greenSoft else colors.surface, ShapePill)
                    .clickable { onSelect(destination) }
                    .padding(vertical = Spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (selected) destination.selectedIcon else destination.icon,
                    contentDescription = destination.label,
                    tint = if (selected) colors.greenDeep else colors.inkMuted,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = destination.label,
                    style = EquilibrioTheme.typography.caption,
                    color = if (selected) colors.greenDeep else colors.inkMuted,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqBottomNavPreview() {
    EquilibrioTheme {
        EqBottomNav(current = EqNavDestination.GOALS, onSelect = {}, modifier = Modifier.fillMaxWidth().padding(Spacing.base))
    }
}
