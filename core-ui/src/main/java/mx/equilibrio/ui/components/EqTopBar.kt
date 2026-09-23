package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget

/**
 * Título (+ back/subtítulo) y `trailing` (el avatar de perfil) viven en DOS bloques
 * independientes dentro de un `Box`, cada uno con su propio espacio — no comparten un
 * `Row` con `weight()`. Así ninguno puede invadir al otro por más que su contenido
 * cambie de tamaño (nombre largo, imagen de perfil que carga tarde, etc.).
 */
@Composable
fun EqTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = TouchTarget.minRowHeight),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(horizontal = Spacing.base, vertical = Spacing.md)
                .padding(end = if (trailing != null) TouchTarget.minSize + Spacing.sm else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Cerrar", tint = EquilibrioTheme.colors.ink)
                }
            }
            Column {
                Text(
                    text = title,
                    style = EquilibrioTheme.typography.h1,
                    color = EquilibrioTheme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = EquilibrioTheme.typography.bodySmall,
                        color = EquilibrioTheme.colors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (trailing != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = Spacing.base, vertical = Spacing.md),
            ) {
                trailing()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqTopBarPreview() {
    EquilibrioTheme {
        EqTopBar(title = "Hola, Diego")
    }
}
