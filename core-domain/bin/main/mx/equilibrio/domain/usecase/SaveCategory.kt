package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.repository.CategoryRepository
import javax.inject.Inject

class SaveCategory @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(category: Category) = repository.upsert(category)
}
