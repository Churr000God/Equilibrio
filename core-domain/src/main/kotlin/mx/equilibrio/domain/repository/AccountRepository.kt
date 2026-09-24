package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Account

interface AccountRepository {
    fun observeAll(): Flow<List<Account>>
    /** Cuentas BANK/CREDIT_CARD activas; las CASH no consumen cupo del plan FREE. */
    suspend fun countNonCash(): Int
    suspend fun getById(id: String): Account?
    suspend fun upsert(account: Account)
    suspend fun delete(id: String)
}
