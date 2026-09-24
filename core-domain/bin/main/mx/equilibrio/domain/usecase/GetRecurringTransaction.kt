package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class GetRecurringTransaction @Inject constructor(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(id: String): RecurringTransaction? = repository.getById(id)
}
