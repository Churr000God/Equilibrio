package mx.equilibrio.app.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.equilibrio.domain.usecase.RegisterWithPasswordUseCase
import mx.equilibrio.domain.usecase.SignInWithGoogleUseCase
import mx.equilibrio.domain.usecase.SignInWithPasswordUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithPassword: SignInWithPasswordUseCase,
    private val registerWithPassword: RegisterWithPasswordUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onModeChanged(mode: AuthMode) = _state.update {
        it.copy(
            mode = mode,
            nameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            submitError = null,
        )
    }

    fun onNameChanged(value: String) = _state.update { it.copy(name = value, nameError = null, submitError = null) }
    fun onEmailChanged(value: String) = _state.update { it.copy(email = value, emailError = null, submitError = null) }
    fun onPasswordChanged(value: String) = _state.update {
        it.copy(password = value, passwordError = null, submitError = null)
    }
    fun onConfirmPasswordChanged(value: String) = _state.update {
        it.copy(confirmPassword = value, confirmPasswordError = null, submitError = null)
    }

    fun onSubmit() {
        val current = _state.value
        val invalid = current.validate()
        if (invalid != null) {
            _state.update { invalid }
            return
        }

        _state.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            val result = when (current.mode) {
                AuthMode.LOGIN -> signInWithPassword(current.email.trim(), current.password)
                AuthMode.REGISTER -> registerWithPassword(
                    displayName = current.name.trim(),
                    email = current.email.trim(),
                    password = current.password,
                )
            }

            result
                .onSuccess { _state.update { it.copy(isSubmitting = false, authenticated = true) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(isSubmitting = false, submitError = error.message ?: "No se pudo completar la solicitud.")
                    }
                }
        }
    }

    fun onGoogleSignIn(context: Context) {
        // Evita relanzar Credential Manager si ya hay un intento en curso (p. ej. doble tap).
        if (_state.value.isGoogleSigningIn) return
        _state.update { it.copy(isGoogleSigningIn = true, googleError = null) }

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
                    _state.update { it.copy(isGoogleSigningIn = false, authenticated = true) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isGoogleSigningIn = false,
                            googleError = error.message ?: "No se pudo iniciar sesión con Google.",
                        )
                    }
                }
        }
    }
}
