package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountCountUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(): Int = repository.count()
}
