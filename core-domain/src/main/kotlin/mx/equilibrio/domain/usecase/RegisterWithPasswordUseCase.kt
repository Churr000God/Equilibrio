package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class RegisterWithPasswordUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(displayName: String?, email: String, password: String): Result<User> =
        repository.registerWithPassword(displayName, email, password)
}
