package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.RecurringTransaction

interface RecurringTransactionRepository {
    fun observeAll(): Flow<List<RecurringTransaction>>
    suspend fun getById(id: String): RecurringTransaction?
    suspend fun upsert(series: RecurringTransaction)
    suspend fun setActive(id: String, isActive: Boolean)

    /** Soft-delete SOLO de la plantilla — no toca las transacciones ya generadas. */
    suspend fun delete(id: String)
}
