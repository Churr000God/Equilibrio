package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE google_id = :googleId LIMIT 1")
    suspend fun getByGoogleId(googleId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Upsert
    suspend fun upsert(entity: UserEntity)
}
