package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Period

interface PeriodRepository {
    fun observeByAccount(accountId: String): Flow<List<Period>>

    /** Periodos con `state != CLOSED` de la cuenta, ordenados por `startAt` ascendente. */
    suspend fun getActiveByAccount(accountId: String): List<Period>

    suspend fun getById(id: String): Period?

    suspend fun upsert(period: Period)
}
