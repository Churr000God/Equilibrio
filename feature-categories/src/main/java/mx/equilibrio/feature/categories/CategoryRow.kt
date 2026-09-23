package mx.equilibrio.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.EquilibrioColors
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget
import kotlin.math.roundToInt

/** (profundo, base, medio) de la familia de dominio que le toca a este tipo de categoría. */
private fun categoryPalette(type: CategoryType, colors: EquilibrioColors): Triple<Color, Color, Color> =
    when (type) {
        CategoryType.INCOME -> Triple(colors.greenDeep, colors.green, colors.greenMid)
        CategoryType.EXPENSE -> Triple(colors.purpleDeep, colors.purple, colors.purpleMid)
    }

/** Color sólido del avatar. A diferencia de accountGradient, las categorías no llevan degradado. */
internal fun categoryColor(type: CategoryType, colorSlot: Int, colors: EquilibrioColors): Color {
    val (deep, base, mid) = categoryPalette(type, colors)
    return when (((colorSlot % 3) + 3) % 3) {
        0 -> base
        1 -> deep
        else -> mid
    }
}

/** Fila de categoría: ícono, nombre + movimientos, monto + % del periodo. */
@Composable
fun CategoryRow(category: CategoryUi, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val colors = EquilibrioTheme.colors
    val tint = categoryColor(category.type, category.colorSlot, colors)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.minRowHeight)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = Spacing.base, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            val icon = CategoryIcons.get(category.icon)
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = category.name.firstOrNull()?.uppercase() ?: "?",
                    style = EquilibrioTheme.typography.label,
                    color = tint,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = category.name, style = EquilibrioTheme.typography.body, color = colors.ink)
            Text(
                text = "${category.movementsCount} movimientos",
                style = EquilibrioTheme.typography.caption,
                color = colors.inkMuted,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatCents(category.amountCents), style = EquilibrioTheme.typography.bodyStrong, color = colors.ink)
            Text(
                text = "${(category.share * 100).roundToInt()}%",
                style = EquilibrioTheme.typography.caption,
                color = colors.inkMuted,
                textAlign = TextAlign.End,
            )
        }
        if (onClick != null) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.inkFaint, modifier = Modifier.size(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryRowPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            CategoryRow(
                category = CategoryUi(
                    id = "1", name = "Comida", type = CategoryType.EXPENSE, colorSlot = 0, icon = "restaurant",
                    amountCents = 342_000, movementsCount = 24, share = 0.28f,
                ),
                onClick = {},
            )
            CategoryRow(
                category = CategoryUi(
                    id = "2", name = "Salario", type = CategoryType.INCOME, colorSlot = 1, icon = "",
                    amountCents = 1_850_000, movementsCount = 2, share = 0.62f,
                ),
                onClick = {},
            )
        }
    }
}
