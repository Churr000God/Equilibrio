package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.AlertEntity

@Dao
interface AlertDao {

    @Insert
    suspend fun insert(alert: AlertEntity)

    @Query("UPDATE alerts SET is_read = 1, updated_at = :now WHERE id = :alertId")
    suspend fun markAsRead(alertId: String, now: Long)

    @Query(
        "SELECT * FROM alerts WHERE user_id = :userId AND is_read = 0 AND is_deleted = 0 ORDER BY triggered_at DESC",
    )
    fun getPendingAlertsByUser(userId: String): Flow<List<AlertEntity>>

    @Query(
        "SELECT * FROM alerts WHERE user_id = :userId AND type = :type AND is_read = 0 AND is_deleted = 0 LIMIT 1",
    )
    suspend fun getActiveByType(userId: String, type: String): AlertEntity?

    @Query("UPDATE alerts SET is_deleted = 1, updated_at = :now WHERE id = :alertId")
    suspend fun softDelete(alertId: String, now: Long)
}
