package mx.equilibrio.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.GoalEntity

/** Meta más la suma de sus abonos vivos (movimientos con `goal_id`) — un solo viaje a SQLite. */
data class GoalWithSaved(
    @Embedded val goal: GoalEntity,
    @ColumnInfo(name = "saved_cents") val savedCents: Long,
)

@Dao
interface GoalDao {

    @Query(
        """
        SELECT g.*, COALESCE(SUM(t.amount_cents), 0) AS saved_cents
        FROM goals g
        LEFT JOIN transactions t ON t.goal_id = g.id AND t.is_deleted = 0 AND t.status = 'COMPLETED'
        WHERE g.user_id = :userId AND g.is_deleted = 0
        GROUP BY g.id
        ORDER BY g.created_at DESC
        """,
    )
    fun observeAll(userId: String): Flow<List<GoalWithSaved>>

    @Query(
        """
        SELECT g.*, COALESCE(SUM(t.amount_cents), 0) AS saved_cents
        FROM goals g
        LEFT JOIN transactions t ON t.goal_id = g.id AND t.is_deleted = 0 AND t.status = 'COMPLETED'
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

    /** Fechas de abonos vivos a metas vivas del usuario, para la racha. */
    @Query(
        """
        SELECT t.occurred_at FROM transactions t
        INNER JOIN goals g ON g.id = t.goal_id
        WHERE g.user_id = :userId AND t.is_deleted = 0 AND t.status = 'COMPLETED' AND g.is_deleted = 0
        """,
    )
    fun observeContributionDates(userId: String): Flow<List<Long>>
}
