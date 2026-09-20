package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.GoalRepository
import javax.inject.Inject

class DeleteGoal @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
