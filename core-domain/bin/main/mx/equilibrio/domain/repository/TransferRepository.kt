package mx.equilibrio.domain.repository

import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.Transfer

interface TransferRepository {
    suspend fun create(expense: Transaction, income: Transaction): Transfer
    suspend fun findByTransactionId(transactionId: String): Transfer?
}
