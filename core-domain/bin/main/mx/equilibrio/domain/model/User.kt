package mx.equilibrio.domain.model

data class User(
    val id: String,
    val googleId: String?,
    val email: String?,
    val displayName: String?,
    val givenName: String?,
    val familyName: String?,
    val photoUrl: String?,
    val plan: PlanTier,
    val hasPassword: Boolean = false,
) {
    // Autenticado si tiene Google vinculado o password propio; sin ninguno de los dos no puede iniciar sesión.
    val isSignedIn: Boolean get() = googleId != null || hasPassword
}
