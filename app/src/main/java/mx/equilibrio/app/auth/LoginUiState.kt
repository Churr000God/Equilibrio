package mx.equilibrio.app.auth

enum class AuthMode { LOGIN, REGISTER }

data class LoginUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val isGoogleSigningIn: Boolean = false,
    val googleError: String? = null,
    val authenticated: Boolean = false,
) {
    val canSubmit: Boolean get() = !isSubmitting && !isGoogleSigningIn
}

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/** Devuelve el estado con errores llenos, o null si todo pasa. */
fun LoginUiState.validate(): LoginUiState? {
    val emailError = if (!EMAIL_REGEX.matches(email.trim())) "Ingresa un correo válido." else null
    val passwordError = if (password.length < 8) "La contraseña debe tener al menos 8 caracteres." else null
    val nameError = if (mode == AuthMode.REGISTER && name.isBlank()) "Ponle un nombre a tu cuenta." else null
    val confirmPasswordError = if (mode == AuthMode.REGISTER && confirmPassword != password) {
        "Las contraseñas no coinciden."
    } else {
        null
    }

    val hasError = listOf(emailError, passwordError, nameError, confirmPasswordError).any { it != null }
    if (!hasError) return null

    return copy(
        emailError = emailError,
        passwordError = passwordError,
        nameError = nameError,
        confirmPasswordError = confirmPasswordError,
    )
}
