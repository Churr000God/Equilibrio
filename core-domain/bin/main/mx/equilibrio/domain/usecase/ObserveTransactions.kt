package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

class ObserveTransactions @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Transaction>> = repository.observeAll()
}
