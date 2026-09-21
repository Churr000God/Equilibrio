package mx.equilibrio.app.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.equilibrio.ui.components.EqAvatar
import mx.equilibrio.ui.theme.TouchTarget

/**
 * Componente circular de perfil del HUD (RF01), esquina superior derecha del
 * AppBar. Al tocarlo abre [ProfileScreen] como pantalla propia — ya no un
 * bottom sheet.
 */
@Composable
fun ProfileHud(
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .size(TouchTarget.minSize)
            .clip(CircleShape)
            .clickable(onClick = onOpenProfile),
        contentAlignment = Alignment.Center,
    ) {
        EqAvatar(
            photoUrl = user?.photoUrl,
            contentDescription = if (user?.isSignedIn == true) "Tu perfil" else "Cuenta local",
        )
    }
}
