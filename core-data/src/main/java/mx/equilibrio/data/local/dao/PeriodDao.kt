package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.PeriodEntity

@Dao
interface PeriodDao {

    @Query(
        "SELECT * FROM periods WHERE account_id = :accountId AND is_deleted = 0 ORDER BY start_at",
    )
    fun observeByAccount(accountId: String): Flow<List<PeriodEntity>>

    @Query("SELECT * FROM periods WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PeriodEntity?

    @Query(
        "SELECT * FROM periods WHERE account_id = :accountId AND state != 'CLOSED' AND is_deleted = 0 ORDER BY start_at",
    )
    suspend fun getActiveByAccount(accountId: String): List<PeriodEntity>

    @Upsert
    suspend fun upsert(entity: PeriodEntity)

    @Query(
        "UPDATE periods SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id",
    )
    suspend fun markDeleted(id: String, now: Long)
}
