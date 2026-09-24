package mx.equilibrio.app.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.equilibrio.domain.usecase.ObserveAccessibilityModeUseCase
import mx.equilibrio.domain.usecase.ObserveCurrentUser
import mx.equilibrio.domain.usecase.SetAccessibilityModeUseCase
import mx.equilibrio.domain.usecase.SignInWithGoogleUseCase
import mx.equilibrio.domain.usecase.SignOutUseCase
import mx.equilibrio.domain.usecase.UpdateProfileUseCase
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userId: String? = null,
    val isSignedIn: Boolean = false,
    val email: String? = null,
    val displayNameInput: String = "",
    val givenNameInput: String = "",
    val familyNameInput: String = "",
    val photoUrl: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val isSigningIn: Boolean = false,
    val signInError: String? = null,
    val accessibilityMode: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    observeAccessibilityModeUseCase: ObserveAccessibilityModeUseCase,
    private val setAccessibilityModeUseCase: SetAccessibilityModeUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        userId = user?.id,
                        isSignedIn = user?.isSignedIn == true,
                        email = user?.email,
                        displayNameInput = user?.displayName.orEmpty(),
                        givenNameInput = user?.givenName.orEmpty(),
                        familyNameInput = user?.familyName.orEmpty(),
                        photoUrl = user?.photoUrl,
                    )
                }
            }
        }

        viewModelScope.launch {
            observeAccessibilityModeUseCase().collect { enabled ->
                _state.update { it.copy(accessibilityMode = enabled) }
            }
        }
    }

    fun onAccessibilityModeToggled(enabled: Boolean) {
        viewModelScope.launch { setAccessibilityModeUseCase(enabled) }
    }

    fun onDisplayNameChanged(value: String) = _state.update { it.copy(displayNameInput = value, saved = false) }
    fun onGivenNameChanged(value: String) = _state.update { it.copy(givenNameInput = value, saved = false) }
    fun onFamilyNameChanged(value: String) = _state.update { it.copy(familyNameInput = value, saved = false) }

    fun onPhotoPicked(context: Context, uri: Uri) {
        val userId = _state.value.userId ?: return
        viewModelScope.launch {
            val localUri = withContext(Dispatchers.IO) { persistAvatarLocally(context, userId, uri) }
            _state.update { it.copy(photoUrl = localUri, saved = false) }
        }
    }

    fun save() {
        val current = _state.value
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            updateProfileUseCase(
                displayName = current.displayNameInput.trim().ifBlank { null },
                givenName = current.givenNameInput.trim().ifBlank { null },
                familyName = current.familyNameInput.trim().ifBlank { null },
                photoUrl = current.photoUrl,
            )
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }

    fun signIn(context: Context) {
        // Evita relanzar Credential Manager si ya hay un intento en curso (p. ej. doble tap).
        if (_state.value.isSigningIn) return
        _state.update { it.copy(isSigningIn = true, signInError = null) }

        viewModelScope.launch {
            GoogleAuthClient(context).signIn()
                .onSuccess { identity ->
                    signInWithGoogleUseCase(
                        googleId = identity.googleId,
                        email = identity.email,
                        displayName = identity.displayName,
                        givenName = identity.givenName,
                        familyName = identity.familyName,
                        photoUrl = identity.photoUrl,
                    )
                }
                .onFailure { error ->
                    _state.update { it.copy(signInError = error.message ?: "No se pudo iniciar sesión con Google.") }
                }
            _state.update { it.copy(isSigningIn = false) }
        }
    }

    /** Solo builds debug (RF01) — ver [ProfileScreen], el botón está detrás de `BuildConfig.DEBUG`. */
    fun mockSignIn() {
        if (_state.value.isSigningIn) return
        _state.update { it.copy(isSigningIn = true, signInError = null) }

        viewModelScope.launch {
            signInWithGoogleUseCase(
                googleId = MOCK_GOOGLE_ID,
                email = "prueba@equilibrio.mock",
                displayName = "Usuario de Prueba",
                givenName = "Usuario",
                familyName = "Prueba",
                photoUrl = null,
            )
            _state.update { it.copy(isSigningIn = false) }
        }
    }

    fun signOut() {
        viewModelScope.launch { signOutUseCase() }
    }

    fun dismissError() {
        _state.update { it.copy(signInError = null) }
    }

    private companion object {
        const val MOCK_GOOGLE_ID = "mock-debug-user"
    }
}
