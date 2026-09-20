package mx.equilibrio.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.EquilibrioDatabase
import mx.equilibrio.data.local.dao.TransactionDao
import mx.equilibrio.data.local.dao.TransferDao
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.mapper.toEntity
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val db: EquilibrioDatabase,
    private val dao: TransactionDao,
    private val transferDao: TransferDao,
    private val session: LocalSession,
) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> = flow {
        emitAll(dao.observeAll(session.currentUserId()).map { list -> list.map { it.toDomain() } })
    }

    override fun observeBalanceCents(): Flow<Long> = flow {
        emitAll(dao.observeBalanceCents(session.currentUserId()))
    }

    override suspend fun getById(id: String): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun upsert(transaction: Transaction) {
        dao.upsert(
            transaction.toEntity(
                syncState = "PENDING",
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun confirm(id: String) = db.withTransaction {
        val now = System.currentTimeMillis()
        val transfer = transferDao.findByTransactionId(id)
        if (transfer != null) {
            dao.confirm(transfer.egresoId, now)
            dao.confirm(transfer.ingresoId, now)
        } else {
            dao.confirm(id, now)
        }
    }

    override suspend fun delete(id: String) = db.withTransaction {
        val now = System.currentTimeMillis()
        val transfer = transferDao.findByTransactionId(id)
        if (transfer != null) {
            dao.markDeleted(transfer.egresoId, now)
            dao.markDeleted(transfer.ingresoId, now)
            transferDao.markDeleted(transfer.id, now)
        } else {
            dao.markDeleted(id, now)
        }
    }
}
