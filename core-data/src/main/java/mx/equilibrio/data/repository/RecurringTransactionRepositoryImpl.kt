package mx.equilibrio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.dao.RecurringTransactionDao
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.mapper.toEntity
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class RecurringTransactionRepositoryImpl @Inject constructor(
    private val dao: RecurringTransactionDao,
    private val session: LocalSession,
) : RecurringTransactionRepository {

    override fun observeAll(): Flow<List<RecurringTransaction>> = flow {
        emitAll(dao.observeAll(session.currentUserId()).map { list -> list.map { it.toDomain() } })
    }

    override suspend fun getById(id: String): RecurringTransaction? = dao.getById(id)?.toDomain()

    override suspend fun upsert(series: RecurringTransaction) {
        dao.upsert(series.toEntity(syncState = "PENDING", updatedAt = System.currentTimeMillis()))
    }

    override suspend fun setActive(id: String, isActive: Boolean) {
        dao.setActive(id, isActive, now = System.currentTimeMillis())
    }

    /** Solo la plantilla: las ocurrencias ya generadas en `transactions` no se tocan. */
    override suspend fun delete(id: String) {
        dao.markDeleted(id, now = System.currentTimeMillis())
    }
}
