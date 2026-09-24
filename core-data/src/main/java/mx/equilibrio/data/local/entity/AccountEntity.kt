package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
        ),
    ],
    indices = [Index("user_id")],
)
data class AccountEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    val type: String,
    // Montos en centavos (enteros) para evitar errores de punto flotante; aplica a todas
    // las columnas *_cents de esta entidad.
    @ColumnInfo(name = "balance_cents") val balanceCents: Long = 0,
    // Solo aplica a cuentas CREDIT_CARD; null en el resto de los tipos.
    @ColumnInfo(name = "credit_limit_cents") val creditLimitCents: Long? = null,
    // Día del mes de corte del estado de cuenta; null salvo en CREDIT_CARD.
    @ColumnInfo(name = "statement_day") val statementDay: Int? = null,
    // Día del mes límite de pago; null salvo en CREDIT_CARD.
    @ColumnInfo(name = "due_day") val dueDay: Int? = null,
    // Índice a la paleta de colores de la UI, no un valor de color en sí.
    @ColumnInfo(name = "color_slot") val colorSlot: Int = 0,
    // Últimos 4 dígitos de la tarjeta; null si la cuenta no tiene tarjeta asociada.
    @ColumnInfo(name = "last_digits") val lastDigits: Int? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
