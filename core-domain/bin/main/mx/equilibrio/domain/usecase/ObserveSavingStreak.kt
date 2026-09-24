package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.calc.SavingStreakCalculator
import mx.equilibrio.domain.repository.GoalRepository
import javax.inject.Inject

class ObserveSavingStreak @Inject constructor(
    private val repository: GoalRepository,
) {
    /** Semanas consecutivas con abono, calculadas contra `today`. */
    operator fun invoke(today: LocalDate): Flow<Int> =
        repository.observeContributionDates().map { SavingStreakCalculator.weeks(it, today) }
}
