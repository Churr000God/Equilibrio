package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
        ),
    ],
    indices = [Index("user_id")],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    val type: String,
    // Índice a la paleta de colores de la UI, no un valor de color en sí.
    @ColumnInfo(name = "color_slot") val colorSlot: Int,
    val icon: String,
    // Categoría predefinida por la app (p. ej. "Sin categoría"); no la crea el usuario y no se puede borrar.
    @ColumnInfo(name = "is_system") val isSystem: Boolean = false,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    // Montos en centavos. null = sin presupuesto mensual configurado para esta categoría.
    @ColumnInfo(name = "monthly_budget_cents") val monthlyBudgetCents: Long? = null,
)
