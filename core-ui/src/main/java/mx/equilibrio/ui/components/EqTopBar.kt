package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

@Composable
fun EqTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Spacing.base, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Cerrar", tint = EquilibrioTheme.colors.ink)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = EquilibrioTheme.typography.h1,
                color = EquilibrioTheme.colors.ink,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = EquilibrioTheme.typography.bodySmall,
                    color = EquilibrioTheme.colors.inkMuted,
                )
            }
        }
        action?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun EqTopBarPreview() {
    EquilibrioTheme {
        EqTopBar(title = "Hola, Diego")
    }
}
