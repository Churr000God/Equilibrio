package mx.equilibrio.domain.model

/**
 * RF01 — reglas de validación de credenciales, exigidas también fuera de la UI
 * (LoginUiState.validate() las duplica para feedback inmediato) para que ningún
 * caller de UserRepository pueda saltárselas.
 */
private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

const val PASSWORD_MIN_LENGTH = 8

// Acota el costo de hashing/verificación Argon2 ante contraseñas absurdamente largas.
const val PASSWORD_MAX_LENGTH = 128

fun isValidEmailFormat(email: String): Boolean = EMAIL_REGEX.matches(email)

fun isValidPasswordLength(password: String): Boolean =
    password.length in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH
