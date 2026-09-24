package mx.equilibrio.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
import mx.equilibrio.feature.categories.CategoryDetailScreen
import mx.equilibrio.feature.entry.QuickEntryScreen
import mx.equilibrio.feature.goals.GoalEditorScreen
import mx.equilibrio.feature.goals.GoalsScreen
import mx.equilibrio.feature.home.FiltersScreen
import mx.equilibrio.feature.home.HomeDashboardScreen
import mx.equilibrio.feature.home.HomeViewModel
import mx.equilibrio.feature.home.MovementsScreen
import mx.equilibrio.feature.home.SearchScreen
import mx.equilibrio.feature.home.TransactionDetailScreen
import mx.equilibrio.domain.model.report.ReportSection
import mx.equilibrio.feature.reports.ReportsScreen
import mx.equilibrio.ui.components.EqBottomNav
import mx.equilibrio.ui.components.EqNavDestination
import mx.equilibrio.ui.theme.EquilibrioTheme

private const val ROUTE_LOGIN = "login"
private const val ROUTE_HOME = "home"
private const val ROUTE_MOVEMENTS = "movements"
private const val ARG_ACCOUNT_ID = "accountId"
private const val ROUTE_ACCOUNTS_BASE = "accounts"
private const val ROUTE_ACCOUNTS = "$ROUTE_ACCOUNTS_BASE?$ARG_ACCOUNT_ID={$ARG_ACCOUNT_ID}"

private fun accountsRoute(accountId: String? = null) =
    ROUTE_ACCOUNTS_BASE + if (accountId != null) "?$ARG_ACCOUNT_ID=$accountId" else ""

private const val ROUTE_ADD_ACCOUNT = "add_account"
private const val ROUTE_CATEGORIES = "categories"
private const val ARG_CATEGORY_ID = "categoryId"
private const val ROUTE_ADD_CATEGORY = "add_category?categoryId={$ARG_CATEGORY_ID}"

private fun addCategoryRoute(categoryId: String? = null) =
    "add_category" + if (categoryId != null) "?categoryId=$categoryId" else ""

private const val ROUTE_CATEGORY_DETAIL = "category_detail/{$ARG_CATEGORY_ID}"

private fun categoryDetailRoute(categoryId: String) = "category_detail/$categoryId"
private const val ROUTE_GOALS = "goals"
private const val ARG_REPORT_SECTION = "section"
private const val ROUTE_REPORTS_BASE = "reports"
private const val ROUTE_REPORTS = "$ROUTE_REPORTS_BASE?$ARG_REPORT_SECTION={$ARG_REPORT_SECTION}"

private fun reportsRoute(section: ReportSection? = null) =
    ROUTE_REPORTS_BASE + if (section != null) "?$ARG_REPORT_SECTION=${section.name}" else ""

private const val ROUTE_PROFILE = "profile"
private const val ARG_TRANSACTION_ID = "transactionId"
private const val ROUTE_QUICK_ENTRY = "quick_entry?transactionId={$ARG_TRANSACTION_ID}"

private fun quickEntryRoute(transactionId: String? = null) =
    "quick_entry" + if (transactionId != null) "?transactionId=$transactionId" else ""

private const val ROUTE_TRANSACTION_DETAIL = "transaction_detail/{$ARG_TRANSACTION_ID}"

private fun transactionDetailRoute(transactionId: String) = "transaction_detail/$transactionId"

private const val ROUTE_FILTERS = "movements_filters"
private const val ROUTE_SEARCH = "movements_search"

private const val ARG_GOAL_ID = "goalId"
private const val ROUTE_GOAL_EDITOR = "goal_editor?goalId={$ARG_GOAL_ID}"

private fun goalEditorRoute(goalId: String? = null) =
    "goal_editor" + if (goalId != null) "?goalId=$goalId" else ""

private val ROUTE_TO_DESTINATION = mapOf(
    ROUTE_HOME to EqNavDestination.HOME,
    ROUTE_MOVEMENTS to EqNavDestination.TRANSACTIONS,
    ROUTE_ACCOUNTS to EqNavDestination.ACCOUNTS,
    ROUTE_CATEGORIES to EqNavDestination.CATEGORIES,
    ROUTE_GOALS to EqNavDestination.GOALS,
    ROUTE_REPORTS to EqNavDestination.REPORTS,
)

private val DESTINATION_TO_ROUTE = mapOf(
    EqNavDestination.HOME to ROUTE_HOME,
    EqNavDestination.TRANSACTIONS to ROUTE_MOVEMENTS,
    EqNavDestination.ACCOUNTS to ROUTE_ACCOUNTS_BASE,
    EqNavDestination.CATEGORIES to ROUTE_CATEGORIES,
    EqNavDestination.GOALS to ROUTE_GOALS,
    EqNavDestination.REPORTS to ROUTE_REPORTS_BASE,
)

/**
 * Desde Inicio a otra pestaña, con el mismo back stack que la barra inferior.
 * Sin restoreState: si no, el estado guardado de la pestaña pisaría el argumento nuevo.
 */
private fun NavHostController.navigateToTabFromHome(route: String) {
    navigate(route) {
        popUpTo(ROUTE_HOME) { saveState = true }
        launchSingleTop = true
    }
}

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
                        // Inicio es la raíz: se vuelve sacando lo que esté encima. Con navigate + restoreState
                        // se restauraba "movements" (también bajo la pestaña Inicio) y el toque no hacía nada.
                        if (destination == EqNavDestination.HOME) {
                            navController.popBackStack(ROUTE_HOME, inclusive = false, saveState = true)
                            return@EqBottomNav
                        }
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
            // Solo el padding inferior (bottomBar) es de este Scaffold externo. Cada pantalla arma su
            // propio EqTopBar como primer hijo de un Column normal (no otro Scaffold anidado) — el
            // orden secuencial de Compose garantiza que el contenido de abajo empiece después del
            // topBar, sin depender de un cálculo de inset que se desincronizaba con el real.
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onAuthenticated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ROUTE_HOME) {
                HomeDashboardScreen(
                    onAddClicked = { navController.navigate(quickEntryRoute()) },
                    onCardsClicked = { navController.navigateToTabFromHome(accountsRoute()) },
                    onCardClicked = { id -> navController.navigateToTabFromHome(accountsRoute(id)) },
                    onAddCardClicked = { navController.navigate(ROUTE_ADD_ACCOUNT) },
                    onBalanceClicked = { navController.navigateToTabFromHome(accountsRoute()) },
                    onSeeMoreClicked = { navController.navigate(ROUTE_MOVEMENTS) },
                    onReportsClicked = { navController.navigateToTabFromHome(reportsRoute()) },
                    onReportClicked = { section -> navController.navigateToTabFromHome(reportsRoute(section)) },
                    trailing = { ProfileHud(onOpenProfile = { navController.navigate(ROUTE_PROFILE) }) },
                )
            }
            composable(ROUTE_MOVEMENTS) {
                MovementsScreen(
                    onAddClicked = { navController.navigate(quickEntryRoute()) },
                    onTransactionClicked = { id -> navController.navigate(transactionDetailRoute(id)) },
                    onSearchClicked = { navController.navigate(ROUTE_SEARCH) },
                    onFiltersClicked = { navController.navigate(ROUTE_FILTERS) },
                    trailing = { ProfileHud(onOpenProfile = { navController.navigate(ROUTE_PROFILE) }) },
                )
            }
            composable(ROUTE_FILTERS) {
                // Comparte el HomeViewModel de Transacciones (mismo back stack entry) en vez de un
                // resultado de navegación — los filtros ya quedan aplicados al volver, es el mismo estado.
                val movementsEntry = remember(navController) { navController.getBackStackEntry(ROUTE_MOVEMENTS) }
                FiltersScreen(
                    onApply = { navController.popBackStack() },
                    onClosed = { navController.popBackStack() },
                    viewModel = hiltViewModel(movementsEntry),
                )
            }
            composable(ROUTE_SEARCH) {
                SearchScreen(
                    onTransactionClicked = { id -> navController.navigate(transactionDetailRoute(id)) },
                    onClosed = { navController.popBackStack() },
                )
            }
            composable(
                route = ROUTE_TRANSACTION_DETAIL,
                arguments = listOf<NamedNavArgument>(
                    navArgument(ARG_TRANSACTION_ID) { type = NavType.StringType },
                ),
            ) {
                TransactionDetailScreen(
                    onEditClicked = { id -> navController.navigate(quickEntryRoute(id)) },
                    onClosed = { navController.popBackStack() },
                )
            }
            composable(
                route = ROUTE_ACCOUNTS,
                arguments = listOf<NamedNavArgument>(
                    navArgument(ARG_ACCOUNT_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
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
                    onAddCategoryClicked = { navController.navigate(addCategoryRoute()) },
                    onCategoryClicked = { id -> navController.navigate(categoryDetailRoute(id)) },
                    trailing = { ProfileHud(onOpenProfile = { navController.navigate(ROUTE_PROFILE) }) },
                )
            }
            composable(
                route = ROUTE_CATEGORY_DETAIL,
                arguments = listOf<NamedNavArgument>(
                    navArgument(ARG_CATEGORY_ID) { type = NavType.StringType },
                ),
            ) {
                CategoryDetailScreen(
                    onEditClicked = { id -> navController.navigate(addCategoryRoute(id)) },
                    onClosed = { navController.popBackStack() },
                )
            }
            composable(ROUTE_PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLoginWithPassword = { navController.navigate(ROUTE_LOGIN) },
                )
            }
            composable(
                route = ROUTE_ADD_CATEGORY,
                arguments = listOf<NamedNavArgument>(
                    navArgument(ARG_CATEGORY_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
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
            composable(
                route = ROUTE_REPORTS,
                arguments = listOf<NamedNavArgument>(
                    navArgument(ARG_REPORT_SECTION) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                val section = entry.arguments?.getString(ARG_REPORT_SECTION)
                    ?.let { name -> ReportSection.entries.firstOrNull { it.name == name } }
                ReportsScreen(focusSection = section)
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
