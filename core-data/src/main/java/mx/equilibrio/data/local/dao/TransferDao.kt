package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import mx.equilibrio.data.local.entity.TransferEntity

@Dao
interface TransferDao {

    @Upsert
    suspend fun upsert(entity: TransferEntity)

    @Query(
        """
        SELECT * FROM transfers
        WHERE (egreso_id = :id OR ingreso_id = :id) AND is_deleted = 0
        LIMIT 1
        """,
    )
    suspend fun findByTransactionId(id: String): TransferEntity?

    @Query(
        "UPDATE transfers SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id",
    )
    suspend fun markDeleted(id: String, now: Long)
}
