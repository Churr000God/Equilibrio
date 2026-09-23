package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
        ),
    ],
    indices = [Index("user_id"), Index("account_id")],
)
data class RecurringTransactionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    val kind: String,
    val classification: String,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    val note: String?,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    val frequency: String,
    @ColumnInfo(name = "anchor_day") val anchorDay: Int? = null,
    @ColumnInfo(name = "next_occurrence_at") val nextOccurrenceAt: Long,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
