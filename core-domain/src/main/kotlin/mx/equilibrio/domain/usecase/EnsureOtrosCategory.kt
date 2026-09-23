package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.domain.repository.CategoryRepository
import javax.inject.Inject

class EnsureOtrosCategory @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(userId: String, type: CategoryType): Category =
        repository.getOrCreateOtros(userId, type)
}
