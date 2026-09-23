package mx.equilibrio.feature.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Set fijo de íconos disponibles para categorías (§10 del manual: Material Symbols Rounded).
 * `Category.icon` guarda la [key]; una key desconocida o vacía cae al avatar de inicial en [CategoryRow].
 */
object CategoryIcons {
    val ALL: List<Pair<String, ImageVector>> = listOf(
        "restaurant" to Icons.Rounded.Restaurant,
        "work" to Icons.Rounded.Work,
        "shopping_cart" to Icons.Rounded.ShoppingCart,
        "directions_car" to Icons.Rounded.DirectionsCar,
        "home" to Icons.Rounded.Home,
        "bolt" to Icons.Rounded.Bolt,
        "apartment" to Icons.Rounded.Apartment,
        "fitness_center" to Icons.Rounded.FitnessCenter,
        "sports_esports" to Icons.Rounded.SportsEsports,
        "flight" to Icons.Rounded.Flight,
        "pets" to Icons.Rounded.Pets,
        "favorite" to Icons.Rounded.Favorite,
        "school" to Icons.Rounded.School,
        "subscriptions" to Icons.Rounded.Subscriptions,
        "payments" to Icons.Rounded.Payments,
        "trending_up" to Icons.Rounded.TrendingUp,
        "local_cafe" to Icons.Rounded.LocalCafe,
        "card_giftcard" to Icons.Rounded.CardGiftcard,
        "more_horiz" to Icons.Rounded.MoreHoriz,
    )

    private val byKey = ALL.toMap()

    fun get(key: String): ImageVector? = byKey[key]
}
