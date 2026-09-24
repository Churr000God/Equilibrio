package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

class GetTransaction @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(id: String): Transaction? = repository.getById(id)
}
