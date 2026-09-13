package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.equilibrio.ui.theme.Elevation
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeLarge
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.eqShadow

@Composable
fun EqCard(
    modifier: Modifier = Modifier,
    elevation: Elevation = Elevation.LEVEL_1,
    hero: Boolean = false,
    padding: androidx.compose.ui.unit.Dp = if (hero) Spacing.lg else Spacing.base,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val shape = if (hero) ShapeLarge else ShapeMedium
    Column(
        modifier = modifier
            .eqShadow(elevation, shape)
            .background(EquilibrioTheme.colors.surface, shape)
            .padding(padding),
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun EqCardPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            EqCard(hero = true) {
                androidx.compose.material3.Text("Tarjeta hero", style = EquilibrioTheme.typography.h2)
            }
        }
    }
}
