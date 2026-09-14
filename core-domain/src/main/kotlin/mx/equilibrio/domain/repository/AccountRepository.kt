package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Account

interface AccountRepository {
    fun observeAll(): Flow<List<Account>>
    suspend fun getById(id: String): Account?
    suspend fun upsert(account: Account)
    suspend fun delete(id: String)
}
