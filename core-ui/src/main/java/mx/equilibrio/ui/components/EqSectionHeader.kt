package mx.equilibrio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.TouchTarget

/** Título de sección con ">" que lleva a la pantalla completa de esa sección. */
@Composable
fun EqSectionHeader(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.minSize)
            .clickable(role = Role.Button, onClickLabel = "Ver $title", onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = EquilibrioTheme.typography.h3, color = colors.ink)
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.inkMuted,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EqSectionHeaderPreview() {
    EquilibrioTheme {
        EqSectionHeader(title = "Tarjetas", onClick = {})
    }
}
