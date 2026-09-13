package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT * FROM transactions
        WHERE user_id = :userId AND is_deleted = 0
        ORDER BY occurred_at DESC
        """,
    )
    fun observeAll(userId: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN kind = 'INCOME' THEN amount_cents ELSE -amount_cents END), 0)
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0
        """,
    )
    fun observeBalanceCents(userId: String): Flow<Long>

    @Query("SELECT * FROM transactions WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Upsert
    suspend fun upsert(entity: TransactionEntity)

    @Query(
        "UPDATE transactions SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id",
    )
    suspend fun markDeleted(id: String, now: Long)
}
