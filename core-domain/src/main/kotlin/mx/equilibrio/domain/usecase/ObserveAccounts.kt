package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.repository.AccountRepository
import javax.inject.Inject

class ObserveAccounts @Inject constructor(
    private val repository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = repository.observeAll()
}
