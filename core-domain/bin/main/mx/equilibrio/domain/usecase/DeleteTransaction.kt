package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransaction @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
