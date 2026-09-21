package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["user_id"]),
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["account_id"]),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["category_id"]),
        ForeignKey(entity = PeriodEntity::class, parentColumns = ["id"], childColumns = ["period_id"]),
    ],
    indices = [
        Index(value = ["user_id", "occurred_at"]),
        Index(value = ["account_id"]),
        Index(value = ["category_id"]),
        Index(value = ["period_id"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    val kind: String,
    val classification: String?,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    @ColumnInfo(name = "occurred_at") val occurredAt: Long,
    val note: String?,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "status") val status: String = "COMPLETED",
    @ColumnInfo(name = "period_id") val periodId: String? = null,
)
