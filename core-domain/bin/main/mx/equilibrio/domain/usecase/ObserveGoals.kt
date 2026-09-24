package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Goal
import mx.equilibrio.domain.repository.GoalRepository
import javax.inject.Inject

class ObserveGoals @Inject constructor(
    private val repository: GoalRepository,
) {
    operator fun invoke(): Flow<List<Goal>> = repository.observeAll()
}
