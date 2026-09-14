package mx.equilibrio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.equilibrio.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE user_id = :userId AND is_deleted = 0 ORDER BY sort_order")
    fun observeAll(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Upsert
    suspend fun upsert(entity: CategoryEntity)

    @Query(
        "UPDATE categories SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id",
    )
    suspend fun markDeleted(id: String, now: Long)
}
