package mx.equilibrio.data.repository

import androidx.room.withTransaction
import java.util.UUID
import mx.equilibrio.data.local.EquilibrioDatabase
import mx.equilibrio.data.local.dao.TransactionDao
import mx.equilibrio.data.local.dao.TransferDao
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.mapper.toEntity
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.Transfer
import mx.equilibrio.domain.repository.TransferRepository
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val db: EquilibrioDatabase,
    private val transferDao: TransferDao,
    private val transactionDao: TransactionDao,
) : TransferRepository {

    override suspend fun create(expense: Transaction, income: Transaction): Transfer = db.withTransaction {
        val now = System.currentTimeMillis()
        transactionDao.upsert(expense.toEntity(syncState = "PENDING", updatedAt = now))
        transactionDao.upsert(income.toEntity(syncState = "PENDING", updatedAt = now))

        val transfer = Transfer(
            id = UUID.randomUUID().toString(),
            expenseTransactionId = expense.id,
            incomeTransactionId = income.id,
        )
        transferDao.upsert(transfer.toEntity(syncState = "PENDING", updatedAt = now))
        transfer
    }

    override suspend fun findByTransactionId(transactionId: String): Transfer? =
        transferDao.findByTransactionId(transactionId)?.toDomain()
}
