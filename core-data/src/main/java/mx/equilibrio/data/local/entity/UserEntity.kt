package mx.equilibrio.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index("google_id"), Index(value = ["email"], unique = true)],
)
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    // Set solo si el usuario entró con Google; null en cuentas registradas por email/contraseña.
    @ColumnInfo(name = "google_id") val googleId: String? = null,
    val email: String? = null,
    @ColumnInfo(name = "given_name") val givenName: String? = null,
    @ColumnInfo(name = "family_name") val familyName: String? = null,
    @ColumnInfo(name = "photo_url") val photoUrl: String? = null,
    // FREE (con límites, RF10) o el plan de pago; determina qué valida GetAccountCountUseCase y afines.
    val plan: String = "FREE",
    // Hash Argon2id; null en cuentas solo-Google, que no tienen contraseña propia.
    @ColumnInfo(name = "password_hash") val passwordHash: String? = null,
)
