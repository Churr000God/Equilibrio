package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Transaction

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeBalanceCents(): Flow<Long>
    suspend fun getById(id: String): Transaction?
    suspend fun upsert(transaction: Transaction)
    suspend fun delete(id: String)
}
