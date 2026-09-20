package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class SignInWithPasswordUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repository.signInWithPassword(email, password)
}
