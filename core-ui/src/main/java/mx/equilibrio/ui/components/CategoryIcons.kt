package mx.equilibrio.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import mx.equilibrio.domain.model.Category

/** Mapa icono → categoría del manual (§iconografía). */
val Category.icon: ImageVector
    get() = when (this) {
        Category.FOOD -> Icons.Rounded.Restaurant
        Category.TRANSPORT -> Icons.Rounded.DirectionsBus
        Category.GROCERIES -> Icons.Rounded.ShoppingCart
        Category.OUTINGS -> Icons.Rounded.LocalBar
        Category.SUBSCRIPTIONS -> Icons.Rounded.Repeat
        Category.MUSIC -> Icons.Rounded.MusicNote
        Category.SAVINGS -> Icons.Rounded.Savings
        Category.OTHER -> Icons.Rounded.MoreHoriz
    }

val Category.label: String
    get() = when (this) {
        Category.FOOD -> "Comida"
        Category.TRANSPORT -> "Transporte"
        Category.GROCERIES -> "Súper"
        Category.OUTINGS -> "Salidas"
        Category.SUBSCRIPTIONS -> "Suscripciones"
        Category.MUSIC -> "Música"
        Category.SAVINGS -> "Ahorro"
        Category.OTHER -> "Otro"
    }
