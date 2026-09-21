package mx.equilibrio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeMedium
import mx.equilibrio.ui.theme.Spacing

/** Placeholder de carga genérico: filas rectangulares atenuadas, mismo tratamiento en toda la app. */
@Composable
fun EqSkeleton(
    modifier: Modifier = Modifier,
    rows: Int = 4,
    rowHeight: Dp = 72.dp,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.base),
        verticalArrangement = Arrangement.spacedBy(Spacing.base),
    ) {
        repeat(rows) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .background(EquilibrioTheme.colors.border.copy(alpha = 0.4f), ShapeMedium),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqSkeletonPreview() {
    EquilibrioTheme {
        EqSkeleton()
    }
}
