package mx.equilibrio.app.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.app.BuildConfig
import mx.equilibrio.ui.components.EqAvatar
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqButtonVariant
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.Spacing

/**
 * Pantalla propia de perfil (ya no un bottom sheet): foto, nombre de usuario,
 * nombre, apellido — editables localmente — y, debajo, la sección de sesión.
 */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLoginWithPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = EquilibrioTheme.colors

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { viewModel.onPhotoPicked(context, it) } }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        EqTopBar(title = "Perfil", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.base)
                .clipToBounds()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            EqAvatar(
                photoUrl = state.photoUrl,
                size = 112.dp,
                contentDescription = "Foto de perfil",
                modifier = Modifier.padding(top = Spacing.base),
            )
            EqButton(
                text = "Cambiar foto",
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                variant = EqButtonVariant.SECONDARY,
                fullWidth = false,
            )

            state.email?.let {
                Text(it, style = EquilibrioTheme.typography.bodySmall, color = colors.inkMuted)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.base),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                EqTextField(
                    value = state.displayNameInput,
                    onValueChange = viewModel::onDisplayNameChanged,
                    label = "Nombre de usuario",
                )
                EqTextField(
                    value = state.givenNameInput,
                    onValueChange = viewModel::onGivenNameChanged,
                    label = "Nombre",
                )
                EqTextField(
                    value = state.familyNameInput,
                    onValueChange = viewModel::onFamilyNameChanged,
                    label = "Apellido",
                )
            }

            EqButton(
                text = if (state.saved) "Guardado" else "Guardar",
                onClick = viewModel::save,
                loading = state.isSaving,
                modifier = Modifier.padding(top = Spacing.sm),
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.md),
                color = colors.border,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    text = "Accesibilidad",
                    style = EquilibrioTheme.typography.bodyStrong,
                    color = colors.ink,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Daltonismo",
                            style = EquilibrioTheme.typography.body,
                            color = colors.ink,
                        )
                        Text(
                            text = "Añade íconos y refuerzos visuales a los estados y categorías.",
                            style = EquilibrioTheme.typography.caption,
                            color = colors.inkMuted,
                        )
                    }
                    Switch(
                        checked = state.accessibilityMode,
                        onCheckedChange = viewModel::onAccessibilityModeToggled,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (state.isSignedIn) {
                    EqButton(
                        text = "Cerrar sesión",
                        onClick = viewModel::signOut,
                        variant = EqButtonVariant.DESTRUCTIVE_LOW_EMPHASIS,
                    )
                } else {
                    Text(
                        text = "Usas Equilibrio como invitado. Inicia sesión, si quieres, para respaldar tu perfil.",
                        style = EquilibrioTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    state.signInError?.let {
                        Text(it, style = EquilibrioTheme.typography.bodySmall, color = colors.error)
                    }
                    EqButton(
                        text = "Iniciar sesión con Google",
                        onClick = { viewModel.signIn(context) },
                        loading = state.isSigningIn,
                    )
                    EqButton(
                        text = "Usar correo y contraseña",
                        onClick = onLoginWithPassword,
                        variant = EqButtonVariant.SECONDARY,
                    )
                    if (BuildConfig.DEBUG) {
                        EqButton(
                            text = "Simular sign-in (debug)",
                            onClick = viewModel::mockSignIn,
                            variant = EqButtonVariant.SECONDARY,
                            loading = state.isSigningIn,
                        )
                    }
                }
            }
        }
    }
}
