package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccount @Inject constructor(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
