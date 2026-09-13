package mx.equilibrio.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.equilibrio.feature.entry.QuickEntryScreen
import mx.equilibrio.feature.home.HomeScreen

private const val ROUTE_HOME = "home"
private const val ARG_TRANSACTION_ID = "transactionId"
private const val ROUTE_QUICK_ENTRY = "quick_entry?transactionId={$ARG_TRANSACTION_ID}"

private fun quickEntryRoute(transactionId: String? = null) =
    "quick_entry" + if (transactionId != null) "?transactionId=$transactionId" else ""

@Composable
fun EquilibrioNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeScreen(
                onAddClicked = { navController.navigate(quickEntryRoute()) },
                onTransactionClicked = { id -> navController.navigate(quickEntryRoute(id)) },
            )
        }
        composable(
            route = ROUTE_QUICK_ENTRY,
            arguments = listOf<NamedNavArgument>(
                navArgument(ARG_TRANSACTION_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            QuickEntryScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
