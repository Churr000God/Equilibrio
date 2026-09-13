package mx.equilibrio.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeSmall
import mx.equilibrio.ui.theme.Spacing

@Composable
fun EqTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    helperOrError: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
) {
    val colors = EquilibrioTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = singleLine,
            isError = isError,
            enabled = enabled,
            shape = ShapeSmall,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            textStyle = EquilibrioTheme.typography.body,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.info,
                unfocusedBorderColor = colors.border,
                errorBorderColor = colors.error,
                disabledBorderColor = colors.border,
                focusedTextColor = colors.ink,
                unfocusedTextColor = colors.ink,
                disabledTextColor = colors.inkFaint,
                cursorColor = colors.info,
            ),
        )
        if (helperOrError != null) {
            Text(
                text = helperOrError,
                style = EquilibrioTheme.typography.caption,
                color = if (isError) colors.error else colors.inkMuted,
                modifier = Modifier.padding(start = Spacing.md, top = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EqTextFieldPreview() {
    EquilibrioTheme {
        Column(modifier = Modifier.padding(Spacing.base)) {
            EqTextField(value = "", onValueChange = {}, label = "Nota", helperOrError = "Opcional")
            EqTextField(
                value = "",
                onValueChange = {},
                label = "Monto",
                helperOrError = "Agrega un monto para guardar.",
                isError = true,
            )
        }
    }
}
