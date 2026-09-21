package mx.equilibrio.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.equilibrio.app.auth.LoginScreen
import mx.equilibrio.app.auth.ProfileHud
import mx.equilibrio.app.auth.ProfileScreen
import mx.equilibrio.feature.accounts.AccountsScreen
import mx.equilibrio.feature.accounts.AddAccountScreen
import mx.equilibrio.feature.categories.AddCategoryScreen
import mx.equilibrio.feature.categories.CategoriesScreen
import mx.equilibrio.feature.entry.QuickEntryScreen
import mx.equilibrio.feature.goals.GoalEditorScreen
import mx.equilibrio.feature.goals.GoalsScreen
import mx.equilibrio.feature.home.HomeScreen
import mx.equilibrio.feature.reports.ReportsScreen
import mx.equilibrio.ui.components.EqBottomNav
import mx.equilibrio.ui.components.EqNavDestination
import mx.equilibrio.ui.theme.EquilibrioTheme

private const val ROUTE_LOGIN = "login"
private const val ROUTE_HOME = "home"
private const val ROUTE_ACCOUNTS = "accounts"
private const val ROUTE_ADD_ACCOUNT = "add_account"
private const val ROUTE_CATEGORIES = "categories"
private const val ROUTE_ADD_CATEGORY = "add_category"
private const val ROUTE_GOALS = "goals"
private const val ROUTE_REPORTS = "reports"
private const val ROUTE_PROFILE = "profile"
private const val ARG_TRANSACTION_ID = "transactionId"
private const val ROUTE_QUICK_ENTRY = "quick_entry?transactionId={$ARG_TRANSACTION_ID}"

private fun quickEntryRoute(transactionId: String? = null) =
    "quick_entry" + if (transactionId != null) "?transactionId=$transactionId" else ""

private const val ARG_GOAL_ID = "goalId"
private const val ROUTE_GOAL_EDITOR = "goal_editor?goalId={$ARG_GOAL_ID}"

private fun goalEditorRoute(goalId: String? = null) =
    "goal_editor" + if (goalId != null) "?goalId=$goalId" else ""

private val ROUTE_TO_DESTINATION = mapOf(
    ROUTE_HOME to EqNavDestination.HOME,
    ROUTE_ACCOUNTS to EqNavDestination.ACCOUNTS,
    ROUTE_CATEGORIES to EqNavDestination.CATEGORIES,
    ROUTE_GOALS to EqNavDestination.GOALS,
    ROUTE_REPORTS to EqNavDestination.REPORTS,
)

private val DESTINATION_TO_ROUTE = mapOf(
    EqNavDestination.HOME to ROUTE_HOME,
    EqNavDestination.ACCOUNTS to ROUTE_ACCOUNTS,
    EqNavDestination.CATEGORIES to ROUTE_CATEGORIES,
    EqNavDestination.GOALS to ROUTE_GOALS,
    EqNavDestination.REPORTS to ROUTE_REPORTS,
)

@Composable
fun EquilibrioNavHost(navController: NavHostController = rememberNavController()) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val currentDestination = ROUTE_TO_DESTINATION[currentRoute]

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        bottomBar = {
            if (currentDestination != null) {
                EqBottomNav(
                    selected = currentDestination,
                    onSelect = { destination ->
                        val route = DESTINATION_TO_ROUTE[destination] ?: return@EqBottomNav
                        navController.navigate(route) {
                            popUpTo(ROUTE_HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onAuthenticated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ROUTE_HOME) {
                HomeScreen(
                    onAddClicked = { navController.navigate(quickEntryRoute()) },
                    onTransactionClicked = { id -> navController.navigate(quickEntryRoute(id)) },
                    trailing = { ProfileHud(onOpenProfile = { navController.navigate(ROUTE_PROFILE) }) },
                )
            }
            composable(ROUTE_ACCOUNTS) {
                AccountsScreen(
                    onAddAccountClicked = { navController.navigate(ROUTE_ADD_ACCOUNT) },
                    trailing = { ProfileHud(onOpenProfile = { navController.navigate(ROUTE_PROFILE) }) },
                )
            }
            composable(ROUTE_ADD_ACCOUNT) {
                AddAccountScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(ROUTE_CATEGORIES) {
                CategoriesScreen(
                    onAddCategoryClicked = { navController.navigate(ROUTE_ADD_CATEGORY) },
                    trailing = { ProfileHud(onOpenProfile = { navController.navigate(ROUTE_PROFILE) }) },
                )
            }
            composable(ROUTE_PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLoginWithPassword = { navController.navigate(ROUTE_LOGIN) },
                )
            }
            composable(ROUTE_ADD_CATEGORY) {
                AddCategoryScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(ROUTE_GOALS) {
                GoalsScreen(
                    onAddClicked = { navController.navigate(goalEditorRoute()) },
                    onGoalClicked = { id -> navController.navigate(goalEditorRoute(id)) },
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
            composable(ROUTE_REPORTS) {
                ReportsScreen()
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
}
