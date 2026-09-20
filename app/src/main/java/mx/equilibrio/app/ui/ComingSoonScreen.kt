package mx.equilibrio.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import mx.equilibrio.ui.components.EqEmptyState
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme

/**
 * Destino previsto pero no implementado. Se muestra en lugar de ocultar el
 * tab para que la estructura del producto sea legible (plan §7, RF04).
 */
@Composable
fun ComingSoonScreen(title: String, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EquilibrioTheme.colors.background,
        topBar = { EqTopBar(title = title) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            EqEmptyState(
                title = "Disponible pronto",
                body = "Esta sección llega en la siguiente entrega.",
            )
        }
    }
}
