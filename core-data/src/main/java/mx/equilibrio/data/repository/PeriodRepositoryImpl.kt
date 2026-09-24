package mx.equilibrio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.dao.PeriodDao
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.mapper.toEntity
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.repository.PeriodRepository
import javax.inject.Inject

class PeriodRepositoryImpl @Inject constructor(
    private val dao: PeriodDao,
) : PeriodRepository {

    override fun observeByAccount(accountId: String): Flow<List<Period>> =
        dao.observeByAccount(accountId).map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveByAccount(accountId: String): List<Period> =
        dao.getActiveByAccount(accountId).map { it.toDomain() }

    override suspend fun getById(id: String): Period? = dao.getById(id)?.toDomain()

    override suspend fun upsert(period: Period) {
        val now = System.currentTimeMillis()
        // Period (dominio) no tiene createdAt; se preserva el de la fila existente si ya
        // existía, o se usa "now" solo en el insert inicial, para no perder la fecha de
        // creación real del periodo en cada actualización.
        val existing = dao.getById(period.id)
        dao.upsert(
            period.toEntity(
                syncState = "PENDING",
                updatedAt = now,
                createdAt = existing?.createdAt ?: now,
            ),
        )
    }
}
