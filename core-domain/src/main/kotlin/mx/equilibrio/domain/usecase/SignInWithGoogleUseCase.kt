package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(
        googleId: String,
        email: String?,
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User = repository.signInWithGoogle(googleId, email, displayName, givenName, familyName, photoUrl)
}
