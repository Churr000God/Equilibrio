package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategory @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
