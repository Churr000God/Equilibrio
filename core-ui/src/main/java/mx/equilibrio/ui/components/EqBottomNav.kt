package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.eqShadow

enum class EqNavDestination(val label: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Rounded.Home),
    TRANSACTIONS("Transacciones", Icons.Rounded.ReceiptLong),
    ACCOUNTS("Cuentas", Icons.Rounded.CreditCard),
    CATEGORIES("Categorías", Icons.Rounded.Category),
    GOALS("Metas", Icons.Rounded.Flag),
    REPORTS("Reportes", Icons.Rounded.PieChart),
}

/** Barra flotante tipo cápsula, separada de los bordes de pantalla (§ referencia visual). */
@Composable
fun EqBottomNav(
    selected: EqNavDestination,
    onSelect: (EqNavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .eqShadow(Elevation.LEVEL_2, ShapePill)
            .background(EquilibrioTheme.colors.surface, ShapePill)
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EqNavDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            EqBottomNavItem(
                destination = destination,
                isSelected = isSelected,
                onClick = { onSelect(destination) },
                // La pestaña activa (con texto) solo toma el espacio que sobra: en pantallas
                // angostas o con fuente grande su texto se corta en vez de empujar a los demás iconos.
                modifier = if (isSelected) Modifier.weight(1f, fill = false) else Modifier,
            )
        }
    }
}

@Composable
private fun EqBottomNavItem(
    destination: EqNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EquilibrioTheme.colors
    val tint = if (isSelected) colors.green else colors.inkFaint
    val itemBackground = if (isSelected) colors.greenSoft else colors.surface

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Tab
                selected = isSelected
                contentDescription = destination.label
            }
            .background(itemBackground, ShapePill)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(destination.icon, contentDescription = null, tint = tint)
        if (isSelected) {
            Text(
                text = destination.label,
                style = EquilibrioTheme.typography.label,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqBottomNavLightPreview() {
    EquilibrioTheme(darkTheme = false) {
        EqBottomNav(selected = EqNavDestination.ACCOUNTS, onSelect = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EqBottomNavDarkPreview() {
    EquilibrioTheme(darkTheme = true) {
        EqBottomNav(selected = EqNavDestination.HOME, onSelect = {})
    }
}
