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
    val isSignedIn: Boolean get() = googleId != null || hasPassword
}
