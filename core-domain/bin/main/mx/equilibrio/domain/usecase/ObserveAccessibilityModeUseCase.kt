package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class ObserveAccessibilityModeUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeAccessibilityMode()
}
