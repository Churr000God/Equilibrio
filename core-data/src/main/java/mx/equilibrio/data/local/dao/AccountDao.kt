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

    @Query("SELECT COUNT(*) FROM accounts WHERE user_id = :userId AND is_deleted = 0")
    suspend fun count(userId: String): Int

    @Upsert
    suspend fun upsert(entity: AccountEntity)
}
