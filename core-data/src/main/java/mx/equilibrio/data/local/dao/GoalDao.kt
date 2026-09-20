package mx.equilibrio.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.GoalContributionEntity
import mx.equilibrio.data.local.entity.GoalEntity

/** Meta más la suma de sus abonos vivos — un solo viaje a SQLite. */
data class GoalWithSaved(
    @Embedded val goal: GoalEntity,
    @ColumnInfo(name = "saved_cents") val savedCents: Long,
)

@Dao
interface GoalDao {

    @Query(
        """
        SELECT g.*, COALESCE(SUM(c.amount_cents), 0) AS saved_cents
        FROM goals g
        LEFT JOIN goal_contributions c ON c.goal_id = g.id AND c.is_deleted = 0
        WHERE g.user_id = :userId AND g.is_deleted = 0
        GROUP BY g.id
        ORDER BY g.created_at DESC
        """,
    )
    fun observeAll(userId: String): Flow<List<GoalWithSaved>>

    @Query(
        """
        SELECT g.*, COALESCE(SUM(c.amount_cents), 0) AS saved_cents
        FROM goals g
        LEFT JOIN goal_contributions c ON c.goal_id = g.id AND c.is_deleted = 0
        WHERE g.id = :id AND g.is_deleted = 0
        GROUP BY g.id
        """,
    )
    suspend fun getById(id: String): GoalWithSaved?

    @Upsert
    suspend fun upsert(entity: GoalEntity)

    @Query("UPDATE goals SET status = :status, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, now: Long)

    @Query("UPDATE goals SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)

    @Insert
    suspend fun insertContribution(entity: GoalContributionEntity)

    @Query(
        """
        SELECT c.occurred_at FROM goal_contributions c
        INNER JOIN goals g ON g.id = c.goal_id
        WHERE g.user_id = :userId AND c.is_deleted = 0 AND g.is_deleted = 0
        """,
    )
    fun observeContributionDates(userId: String): Flow<List<Long>>

    @Query(
        "UPDATE goal_contributions SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE goal_id = :goalId",
    )
    suspend fun markContributionsDeletedByGoal(goalId: String, now: Long)

    @Query(
        "UPDATE goal_contributions SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE transaction_id = :transactionId",
    )
    suspend fun markContributionDeletedByTransaction(transactionId: String, now: Long)
}
