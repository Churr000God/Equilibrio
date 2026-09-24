package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.TransferEntity
import mx.equilibrio.domain.model.Transfer

// TransferEntity conserva los nombres de columna históricos (egresoId/ingresoId); el
// dominio usa expenseTransactionId/incomeTransactionId. No son campos distintos, es
// solo la traducción de nombre entre persistencia y dominio.
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
