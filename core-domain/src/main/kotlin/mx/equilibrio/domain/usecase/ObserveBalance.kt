package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

class ObserveBalance @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<Long> = repository.observeBalanceCents()
}
