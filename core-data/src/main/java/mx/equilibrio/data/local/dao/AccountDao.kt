package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.AccountEntity

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE user_id = :userId AND is_deleted = 0")
    fun observeAll(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT COUNT(*) FROM accounts WHERE user_id = :userId AND is_deleted = 0 AND type != 'CASH'")
    suspend fun countNonCash(userId: String): Int

    @Query("SELECT * FROM accounts WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): AccountEntity?

    @Upsert
    suspend fun upsert(entity: AccountEntity)

    @Query(
        "UPDATE accounts SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id",
    )
    suspend fun markDeleted(id: String, now: Long)
}
