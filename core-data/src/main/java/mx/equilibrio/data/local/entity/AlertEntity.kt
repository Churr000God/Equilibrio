package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alerts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
        ),
    ],
    indices = [Index("user_id")],
)
data class AlertEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val type: String,
    // Id de la entidad a la que refiere la alerta (cuenta, meta, periodo, etc.) según `type`; null si no aplica.
    @ColumnInfo(name = "reference_id") val referenceId: String? = null,
    // 0/1: a diferencia del resto de las entidades, acá is_read/is_deleted quedaron como Int en
    // vez de Boolean; Room mapea ambos igual a INTEGER, así que el valor se lee igual (0 = false).
    @ColumnInfo(name = "is_read") val isRead: Int = 0,
    @ColumnInfo(name = "triggered_at") val triggeredAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Int = 0,
)
