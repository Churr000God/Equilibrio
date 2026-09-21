package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>

    /**
     * Resuelve el usuario por `googleId`: si no existe, reclama el usuario local
     * anónimo actual (conserva sus cuentas/transacciones); si ya existe, actualiza
     * sus datos de perfil y cambia la sesión activa a ese usuario.
     */
    suspend fun signInWithGoogle(
        googleId: String,
        email: String?,
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User

    suspend fun signOut()

    /** Edición local del perfil (RF01 extendido): nombre/nombre/apellido/foto. */
    suspend fun updateProfile(
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User

    /**
     * RF01 — registro con email/contraseña. Reclama el usuario local anónimo actual
     * (igual que [signInWithGoogle]), le asigna el correo y el hash de la contraseña,
     * y deja la sesión iniciada. Falla si el correo ya está en uso.
     */
    suspend fun registerWithPassword(displayName: String?, email: String, password: String): Result<User>

    /**
     * RF01 — inicio de sesión con email/contraseña. No revela si el correo existe
     * cuando la combinación es incorrecta (mismo mensaje de error en ambos casos).
     */
    suspend fun signInWithPassword(email: String, password: String): Result<User>

    fun observeAccessibilityMode(): Flow<Boolean>

    suspend fun setAccessibilityMode(enabled: Boolean)
}
