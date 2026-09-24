package mx.equilibrio.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapePill
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget

/**
 * PRIMARY: acción principal (relleno verde). SECONDARY: acción secundaria, sin relleno.
 * DESTRUCTIVE: elimina/irreversible, máximo peso visual (relleno de error).
 * DESTRUCTIVE_LOW_EMPHASIS: también irreversible, pero pensada para pesar menos que
 * "Cancelar" en un diálogo de confirmación (ver [EqDestructiveDialog]).
 */
enum class EqButtonVariant { PRIMARY, SECONDARY, DESTRUCTIVE, DESTRUCTIVE_LOW_EMPHASIS }

@Composable
fun EqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EqButtonVariant = EqButtonVariant.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = true,
) {
    val colors = EquilibrioTheme.colors
    val buttonModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .heightIn(min = TouchTarget.minSize)

    when (variant) {
        EqButtonVariant.SECONDARY -> OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = buttonModifier,
            shape = ShapePill,
            border = BorderStroke(1.dp, if (enabled) colors.borderStrong else colors.border),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.ink,
                disabledContentColor = colors.inkFaint,
            ),
        ) {
            EqButtonContent(text, loading)
        }

        // Menor peso visual a propósito: en un diálogo destructivo, "Cancelar"
        // debe pesar más que la acción que borra datos (§6.5 / §11.2).
        EqButtonVariant.DESTRUCTIVE_LOW_EMPHASIS -> OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = buttonModifier,
            shape = ShapePill,
            border = BorderStroke(1.dp, if (enabled) colors.error else colors.border),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.error,
                disabledContentColor = colors.inkFaint,
            ),
        ) {
            EqButtonContent(text, loading)
        }

        else -> {
            val fill = when (variant) {
                EqButtonVariant.DESTRUCTIVE -> colors.error
                else -> colors.green
            }
            Button(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = buttonModifier,
                shape = ShapePill,
                colors = solidButtonColors(fill, colors.border, colors.inkFaint),
            ) {
                EqButtonContent(text, loading)
            }
        }
    }
}

@Composable
private fun solidButtonColors(fill: Color, disabledFill: Color, disabledContent: Color): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = fill,
        contentColor = Color.White,
        disabledContainerColor = disabledFill,
        disabledContentColor = disabledContent,
    )

@Composable
private fun EqButtonContent(text: String, loading: Boolean) {
    if (loading) {
        CircularProgressIndicator(modifier = Modifier.padding(2.dp), color = Color.White, strokeWidth = 2.dp)
    } else if (EquilibrioTheme.accessibilityMode) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text, style = EquilibrioTheme.typography.label)
        }
    } else {
        Text(text, style = EquilibrioTheme.typography.label)
    }
}

@Preview(showBackground = true)
@Composable
private fun EqButtonPreview() {
    EquilibrioTheme {
        Column(
            modifier = Modifier.padding(Spacing.base),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            EqButton("Guardar gasto", {})
            EqButton("Cancelar", {}, variant = EqButtonVariant.SECONDARY)
            EqButton("Eliminar", {}, variant = EqButtonVariant.DESTRUCTIVE)
            EqButton("Guardando…", {}, loading = true)
            EqButton("Deshabilitado", {}, enabled = false)
        }
    }
}
