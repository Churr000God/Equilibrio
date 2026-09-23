package mx.equilibrio.app.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqButtonVariant
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

private val MODE_OPTIONS = listOf("Iniciar sesión" to AuthMode.LOGIN, "Registrarse" to AuthMode.REGISTER)

/** RF01 — opcional: se llega aquí desde Perfil, no es la pantalla de arranque de la app. */
@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = EquilibrioTheme.colors

    LaunchedEffect(state.authenticated) {
        if (state.authenticated) onAuthenticated()
    }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        EqTopBar(title = "Iniciar sesión", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.base)
                .clipToBounds()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            EqSegmentedControl(
                options = MODE_OPTIONS.map { it.first },
                selectedIndex = MODE_OPTIONS.indexOfFirst { it.second == state.mode },
                onSelect = { index -> viewModel.onModeChanged(MODE_OPTIONS[index].second) },
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                if (state.mode == AuthMode.REGISTER) {
                    EqTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChanged,
                        label = "Nombre",
                        isError = state.nameError != null,
                        helperOrError = state.nameError,
                    )
                }

                EqTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Correo",
                    keyboardType = KeyboardType.Email,
                    isError = state.emailError != null,
                    helperOrError = state.emailError,
                )

                EqTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Contraseña",
                    keyboardType = KeyboardType.Password,
                    isError = state.passwordError != null,
                    helperOrError = state.passwordError,
                )

                if (state.mode == AuthMode.REGISTER) {
                    EqTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChanged,
                        label = "Confirmar contraseña",
                        keyboardType = KeyboardType.Password,
                        isError = state.confirmPasswordError != null,
                        helperOrError = state.confirmPasswordError,
                    )
                }

                state.submitError?.let {
                    Text(it, style = EquilibrioTheme.typography.bodySmall, color = colors.error)
                }
            }

            EqButton(
                text = if (state.mode == AuthMode.LOGIN) "Entrar" else "Crear cuenta",
                onClick = viewModel::onSubmit,
                enabled = state.canSubmit,
                loading = state.isSubmitting,
            )

            Text(
                text = "o",
                style = EquilibrioTheme.typography.bodySmall,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            state.googleError?.let {
                Text(it, style = EquilibrioTheme.typography.bodySmall, color = colors.error)
            }

            EqButton(
                text = "Continuar con Google",
                onClick = { viewModel.onGoogleSignIn(context) },
                variant = EqButtonVariant.SECONDARY,
                enabled = state.canSubmit,
                loading = state.isGoogleSigningIn,
                modifier = Modifier.padding(bottom = Spacing.xxl),
            )
        }
    }
}
