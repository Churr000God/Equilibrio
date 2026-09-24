package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke() = repository.signOut()
}
