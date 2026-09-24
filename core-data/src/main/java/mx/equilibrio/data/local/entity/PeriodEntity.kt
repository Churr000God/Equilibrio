package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Periodo de facturación de una cuenta CREDIT_CARD: [startAt, endAt) es el corte, [payAt] la fecha límite de pago. */
@Entity(
    tableName = "periods",
    foreignKeys = [
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["account_id"]),
    ],
    indices = [
        Index(value = ["account_id"]),
    ],
)
data class PeriodEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "start_at") val startAt: Long,
    @ColumnInfo(name = "end_at") val endAt: Long,
    @ColumnInfo(name = "pay_at") val payAt: Long,
    val state: String = "OPEN",
    // Montos en centavos. Saldo que quedó sin pagar del periodo y se traslada al siguiente.
    @ColumnInfo(name = "carried_balance_cents") val carriedBalanceCents: Long = 0,
    @ColumnInfo(name = "amount_paid_cents") val amountPaidCents: Long = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
