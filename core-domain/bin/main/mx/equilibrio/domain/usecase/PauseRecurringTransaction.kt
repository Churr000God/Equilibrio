package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

/** Solo pausa (o reactiva) la plantilla — las ocurrencias ya generadas no se tocan. */
class PauseRecurringTransaction @Inject constructor(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(id: String, isActive: Boolean = false) = repository.setActive(id, isActive)
}
