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
}
