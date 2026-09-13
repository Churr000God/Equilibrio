package mx.equilibrio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Radios (§08). */
object Radii {
    val sm = 12.dp
    val md = 20.dp
    val lg = 24.dp
    val pill = 999.dp
}

val ShapeSmall = RoundedCornerShape(Radii.sm)
val ShapeMedium = RoundedCornerShape(Radii.md)
val ShapeLarge = RoundedCornerShape(Radii.lg)
val ShapePill = RoundedCornerShape(Radii.pill)
