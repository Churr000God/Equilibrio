package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryType

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun getById(id: String): Category?
    suspend fun upsert(category: Category)
    suspend fun delete(id: String)

    /** Devuelve la categoría "Otros" (isSystem=true) de [type] para [userId]; la crea si no existe. */
    suspend fun getOrCreateOtros(userId: String, type: CategoryType): Category
}
