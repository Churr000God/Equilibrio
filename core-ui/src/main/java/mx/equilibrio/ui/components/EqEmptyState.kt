package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

/** Nunca un cero: el estado vacío es una invitación, no un dato (§5.4). */
@Composable
fun EqEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Inbox,
    iconTint: Color = EquilibrioTheme.colors.inkFaint,
    /** Si se da, reemplaza a [icon] por una ilustración de marca (vacíos de pantalla completa). */
    illustration: EqIllustration? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = EquilibrioTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (illustration != null) {
            EqIllustrationImage(illustration, modifier = Modifier.padding(bottom = Spacing.base).size(112.dp))
        } else {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(bottom = Spacing.base).size(48.dp),
            )
        }
        Text(
            text = title,
            style = EquilibrioTheme.typography.h3,
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = EquilibrioTheme.typography.bodySmall,
            color = colors.inkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xs, bottom = if (action != null) Spacing.base else 0.dp),
        )
        action?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun EqEmptyStatePreview() {
    EquilibrioTheme {
        EqEmptyState(
            title = "Aún no registras movimientos",
            body = "Registra tu primer ingreso o gasto para ver tu equilibrio.",
            illustration = EqIllustration.BALANCE,
        )
    }
}
