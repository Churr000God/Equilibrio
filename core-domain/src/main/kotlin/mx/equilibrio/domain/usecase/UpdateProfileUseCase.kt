package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User = repository.updateProfile(displayName, givenName, familyName, photoUrl)
}
