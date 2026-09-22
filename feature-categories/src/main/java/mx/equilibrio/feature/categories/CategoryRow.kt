package mx.equilibrio.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.ui.theme.EquilibrioColors
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget

/** (profundo, base, medio) de la familia de dominio que le toca a este tipo de categoría. */
private fun categoryPalette(type: CategoryType, colors: EquilibrioColors): Triple<Color, Color, Color> =
    when (type) {
        CategoryType.INCOME -> Triple(colors.greenDeep, colors.green, colors.greenMid)
        CategoryType.EXPENSE -> Triple(colors.purpleDeep, colors.purple, colors.purpleMid)
    }

/** Color sólido del avatar. A diferencia de eqAccountGradient, las categorías no llevan degradado. */
internal fun categoryColor(type: CategoryType, colorSlot: Int, colors: EquilibrioColors): Color {
    val (deep, base, mid) = categoryPalette(type, colors)
    return when (((colorSlot % 3) + 3) % 3) {
        0 -> base
        1 -> deep
        else -> mid
    }
}

/** Fila de categoría: avatar circular con la inicial del nombre, nombre y tipo (Ingreso/Gasto). */
@Composable
fun CategoryRow(category: CategoryUi, modifier: Modifier = Modifier) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.minRowHeight)
            .padding(horizontal = Spacing.base, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(categoryColor(category.type, category.colorSlot, colors), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = category.name.firstOrNull()?.uppercase() ?: "?",
                style = EquilibrioTheme.typography.label,
                color = Color.White,
            )
        }
        Column {
            Text(text = category.name, style = EquilibrioTheme.typography.body, color = colors.ink)
            Text(text = category.typeLabel, style = EquilibrioTheme.typography.caption, color = colors.inkMuted)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryRowPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            CategoryRow(
                category = CategoryUi(id = "1", name = "Comida", type = CategoryType.EXPENSE, colorSlot = 0, icon = ""),
            )
            CategoryRow(
                category = CategoryUi(id = "2", name = "Salario", type = CategoryType.INCOME, colorSlot = 1, icon = ""),
            )
        }
    }
}
