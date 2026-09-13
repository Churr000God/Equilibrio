package mx.equilibrio.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import mx.equilibrio.ui.theme.EquilibrioTheme

@Composable
fun EqFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = EquilibrioTheme.colors.green,
        contentColor = Color.White,
    ) {
        Icon(Icons.Rounded.Add, contentDescription = "Agregar movimiento")
    }
}

@Preview(showBackground = true)
@Composable
private fun EqFabPreview() {
    EquilibrioTheme {
        EqFab(onClick = {})
    }
}
