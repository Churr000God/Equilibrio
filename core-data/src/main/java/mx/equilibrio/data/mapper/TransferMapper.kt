package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.TransferEntity
import mx.equilibrio.domain.model.Transfer

fun TransferEntity.toDomain(): Transfer = Transfer(
    id = id,
    expenseTransactionId = egresoId,
    incomeTransactionId = ingresoId,
)

fun Transfer.toEntity(syncState: String, updatedAt: Long, isDeleted: Boolean = false): TransferEntity = TransferEntity(
    id = id,
    egresoId = expenseTransactionId,
    ingresoId = incomeTransactionId,
    updatedAt = updatedAt,
    syncState = syncState,
    isDeleted = isDeleted,
)
