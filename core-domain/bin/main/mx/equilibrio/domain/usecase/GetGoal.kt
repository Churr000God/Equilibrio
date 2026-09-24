package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.Goal
import mx.equilibrio.domain.repository.GoalRepository
import javax.inject.Inject

class GetGoal @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String): Goal? = repository.getById(id)
}
