package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class SetAccessibilityModeUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAccessibilityMode(enabled)
}
