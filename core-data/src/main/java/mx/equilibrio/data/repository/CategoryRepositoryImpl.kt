package mx.equilibrio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.dao.CategoryDao
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.mapper.toEntity
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.repository.CategoryRepository
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val session: LocalSession,
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> = flow {
        emitAll(dao.observeAll(session.currentUserId()).map { list -> list.map { it.toDomain() } })
    }

    override suspend fun getById(id: String): Category? = dao.getById(id)?.toDomain()

    override suspend fun upsert(category: Category) {
        dao.upsert(
            category.toEntity(
                syncState = "PENDING",
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun delete(id: String) {
        dao.markDeleted(id, now = System.currentTimeMillis())
    }
}
