package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.CategoryRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteCategory @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) {
    /**
     * Si [reassignToCategoryId] no es null, reasigna los movimientos a esa categoría antes de
     * borrar. Si es null, borra en cascada todos los movimientos de la categoría.
     */
    suspend operator fun invoke(id: String, reassignToCategoryId: String?) {
        if (reassignToCategoryId != null) {
            transactionRepository.reassignCategory(id, reassignToCategoryId)
        } else {
            transactionRepository.deleteByCategory(id)
        }
        categoryRepository.delete(id)
    }
}
