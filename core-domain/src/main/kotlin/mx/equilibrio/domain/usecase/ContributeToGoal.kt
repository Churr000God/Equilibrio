package mx.equilibrio.domain.usecase

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.repository.GoalRepository
import javax.inject.Inject

class ContributeToGoal @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(goalId: String, accountId: String, amountCents: Long, date: LocalDate) {
        require(amountCents > 0) { "El abono debe ser positivo" }
        repository.contribute(goalId, accountId, amountCents, date)
    }
}
