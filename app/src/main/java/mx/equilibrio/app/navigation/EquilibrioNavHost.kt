package mx.equilibrio.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.equilibrio.app.ui.ComingSoonScreen
import mx.equilibrio.feature.entry.QuickEntryScreen
import mx.equilibrio.feature.goals.GoalEditorScreen
import mx.equilibrio.feature.goals.GoalsScreen
import mx.equilibrio.feature.home.HomeScreen
import mx.equilibrio.feature.reports.ReportsScreen
import mx.equilibrio.ui.components.EqBottomNav
import mx.equilibrio.ui.components.EqNavDestination
import mx.equilibrio.ui.theme.Spacing

private const val ARG_TRANSACTION_ID = "transactionId"
private const val ROUTE_QUICK_ENTRY = "quick_entry?transactionId={$ARG_TRANSACTION_ID}"
private const val ARG_GOAL_ID = "goalId"
private const val ROUTE_GOAL_EDITOR = "goal_editor?goalId={$ARG_GOAL_ID}"

private fun goalEditorRoute(goalId: String? = null) =
    "goal_editor" + if (goalId != null) "?goalId=$goalId" else ""

private fun quickEntryRoute(transactionId: String? = null) =
    "quick_entry" + if (transactionId != null) "?transactionId=$transactionId" else ""

/** Ruta de cada tab; los destinos full-screen (registro, editor) no están aquí. */
private val EqNavDestination.route: String
    get() = when (this) {
        EqNavDestination.HOME -> "home"
        EqNavDestination.ACCOUNTS -> "accounts"
        EqNavDestination.GOALS -> "goals"
        EqNavDestination.REPORTS -> "reports"
    }

private fun tabFor(route: String?): EqNavDestination? =
    EqNavDestination.entries.firstOrNull { it.route == route }

@Composable
fun EquilibrioNavHost(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentTab = tabFor(backStack?.destination?.route)

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = EqNavDestination.HOME.route) {
            composable(EqNavDestination.HOME.route) {
                HomeScreen(
                    onAddClicked = { navController.navigate(quickEntryRoute()) },
                    onTransactionClicked = { id -> navController.navigate(quickEntryRoute(id)) },
                    bottomInset = TabBarInset,
                )
            }
            composable(EqNavDestination.ACCOUNTS.route) { ComingSoonScreen(title = "Tus cuentas") }
            composable(EqNavDestination.GOALS.route) {
                GoalsScreen(
                    onAddClicked = { navController.navigate(goalEditorRoute()) },
                    onGoalClicked = { id -> navController.navigate(goalEditorRoute(id)) },
                    bottomInset = TabBarInset,
                )
            }
            composable(EqNavDestination.REPORTS.route) { ReportsScreen(bottomInset = TabBarInset) }
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
            composable(
                route = ROUTE_GOAL_EDITOR,
                arguments = listOf<NamedNavArgument>(
                    navArgument(ARG_GOAL_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                GoalEditorScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }
        }

        if (currentTab != null) {
            EqBottomNav(
                current = currentTab,
                onSelect = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = Spacing.sm),
            )
        }
    }
}

/** Espacio que las pantallas con tab reservan al final para no quedar bajo la barra. */
val TabBarInset = 96.dp
