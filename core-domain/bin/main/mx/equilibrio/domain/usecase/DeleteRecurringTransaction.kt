package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

/** Borra solo la plantilla — las ocurrencias ya generadas no se tocan, el dinero ya se movió. */
class DeleteRecurringTransaction @Inject constructor(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
