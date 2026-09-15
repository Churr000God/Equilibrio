package mx.equilibrio.feature.categories

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqFab
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme

@Composable
fun CategoriesScreen(
    onAddCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = { EqTopBar(title = "Categorías", trailing = trailing) },
        floatingActionButton = { EqFab(onClick = onAddCategoryClicked) },
    ) { padding ->
        when {
            state.isEmpty -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EqEmptyState(
                    title = "Aún no tienes categorías",
                    body = "Agrega categorías para organizar tus ingresos y gastos.",
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            ) {
                items(state.categories, key = { it.id }) { category ->
                    CategoryRow(category = category)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenEmptyPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            EqEmptyState(
                title = "Aún no tienes categorías",
                body = "Agrega categorías para organizar tus ingresos y gastos.",
            )
        }
    }
}
