package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Liga las dos transacciones que forman una transferencia entre cuentas propias. */
@Entity(
    tableName = "transfers",
    foreignKeys = [
        ForeignKey(entity = TransactionEntity::class, parentColumns = ["id"], childColumns = ["egreso_id"]),
        ForeignKey(entity = TransactionEntity::class, parentColumns = ["id"], childColumns = ["ingreso_id"]),
    ],
    indices = [
        Index(value = ["egreso_id"]),
        Index(value = ["ingreso_id"]),
    ],
)
data class TransferEntity(
    @PrimaryKey val id: String,
    // Id de la transacción EXPENSE en la cuenta origen.
    @ColumnInfo(name = "egreso_id") val egresoId: String,
    // Id de la transacción INCOME en la cuenta destino.
    @ColumnInfo(name = "ingreso_id") val ingresoId: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
