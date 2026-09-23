package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.RecurringTransactionEntity

@Dao
interface RecurringTransactionDao {

    @Query("SELECT * FROM recurring_transactions WHERE user_id = :userId AND is_deleted = 0 ORDER BY next_occurrence_at ASC")
    fun observeAll(userId: String): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): RecurringTransactionEntity?

    @Upsert
    suspend fun upsert(entity: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET is_active = :isActive, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun setActive(id: String, isActive: Boolean, now: Long)

    @Query("UPDATE recurring_transactions SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)
}
