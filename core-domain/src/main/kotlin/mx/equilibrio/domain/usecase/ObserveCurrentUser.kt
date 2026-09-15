package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class ObserveCurrentUser @Inject constructor(
    private val repository: UserRepository,
) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUser()
}
