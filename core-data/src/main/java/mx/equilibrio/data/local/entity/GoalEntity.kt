package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Sin `saved_cents`: el avance se deriva de `goal_contributions`. */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
        ),
    ],
    indices = [Index("user_id")],
)
data class GoalEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    // Monto en centavos.
    @ColumnInfo(name = "target_cents") val targetCents: Long,
    // null = meta sin fecha límite.
    val deadline: Long?,
    val status: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
