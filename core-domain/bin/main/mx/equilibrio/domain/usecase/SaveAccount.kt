package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.repository.AccountRepository
import javax.inject.Inject

class SaveAccount @Inject constructor(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(account: Account) = repository.upsert(account)
}
