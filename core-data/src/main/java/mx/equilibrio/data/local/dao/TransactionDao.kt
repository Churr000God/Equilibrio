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
        SELECT * FROM transactions
        WHERE user_id = :userId AND category_id = :categoryId AND is_deleted = 0
        ORDER BY occurred_at DESC
        """,
    )
    fun observeByCategory(userId: String, categoryId: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE recurring_series_id = :seriesId AND is_deleted = 0
        ORDER BY occurred_at DESC
        """,
    )
    fun observeByRecurringSeries(seriesId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Upsert
    suspend fun upsert(entity: TransactionEntity)

    @Query(
        "UPDATE transactions SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id",
    )
    suspend fun markDeleted(id: String, now: Long)

    @Query(
        "UPDATE transactions SET is_deleted = 1, updated_at = :now, sync_state = 'PENDING' WHERE installment_plan_id = :planId",
    )
    suspend fun markInstallmentPlanDeleted(planId: String, now: Long)

    @Query(
        "UPDATE transactions SET category_id = :toCategoryId, sync_state = 'PENDING', updated_at = :now " +
            "WHERE category_id = :fromCategoryId AND is_deleted = 0",
    )
    suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String, now: Long)

    @Query(
        "UPDATE transactions SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now " +
            "WHERE category_id = :categoryId AND is_deleted = 0",
    )
    suspend fun deleteByCategory(categoryId: String, now: Long)

    // Confirma un movimiento SCHEDULED: pasa a COMPLETED y, si su fecha original quedó en el
    // futuro respecto a hoy, la recorta a hoy (no puede impactar el balance con fecha futura).
    @Query(
        """
        UPDATE transactions
        SET status = 'COMPLETED',
            occurred_at = CASE WHEN occurred_at > :todayMillis THEN :todayMillis ELSE occurred_at END,
            sync_state = 'PENDING',
            updated_at = :now
        WHERE id = :id
        """,
    )
    suspend fun confirm(id: String, now: Long, todayMillis: Long)
}
