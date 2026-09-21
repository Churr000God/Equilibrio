package mx.equilibrio.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PeriodTotalRow(
    val kind: String,
    val classification: String?,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
)

data class MonthlyRow(
    /** `YYYY-MM` según `strftime`. */
    val month: String,
    val kind: String,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
)

data class CategoryRow(
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
)

/**
 * Consultas agregadas sobre `transactions`. SQLite agrega y devuelve pocas
 * filas; nunca se recorre el historial en Kotlin (RNF03). Solo movimientos
 * completados; los abonos a metas (`goal_id`) se excluyen de todo salvo
 * `observeSavingsCents`.
 */
@Dao
interface ReportDao {

    @Query(
        """
        SELECT kind, classification, SUM(amount_cents) AS total_cents
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0 AND status = 'COMPLETED'
          AND occurred_at BETWEEN :from AND :to
          AND goal_id IS NULL
        GROUP BY kind, classification
        """,
    )
    fun observePeriodTotals(userId: String, from: Long, to: Long): Flow<List<PeriodTotalRow>>

    @Query(
        """
        SELECT strftime('%Y-%m', occurred_at / 1000, 'unixepoch') AS month, kind, SUM(amount_cents) AS total_cents
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0 AND status = 'COMPLETED'
          AND occurred_at BETWEEN :from AND :to
          AND goal_id IS NULL
        GROUP BY month, kind
        ORDER BY month ASC
        """,
    )
    fun observeMonthlyTrend(userId: String, from: Long, to: Long): Flow<List<MonthlyRow>>

    @Query(
        """
        SELECT t.category_id AS category_id, c.name AS category_name, SUM(t.amount_cents) AS total_cents
        FROM transactions t
        LEFT JOIN categories c ON c.id = t.category_id
        WHERE t.user_id = :userId AND t.is_deleted = 0 AND t.status = 'COMPLETED' AND t.kind = 'EXPENSE'
          AND t.occurred_at BETWEEN :from AND :to
          AND t.goal_id IS NULL
        GROUP BY t.category_id
        ORDER BY total_cents DESC
        """,
    )
    fun observeCategoryTotals(userId: String, from: Long, to: Long): Flow<List<CategoryRow>>

    @Query(
        """
        SELECT COALESCE(SUM(amount_cents), 0)
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0 AND status = 'COMPLETED'
          AND occurred_at BETWEEN :from AND :to
          AND goal_id IS NOT NULL
        """,
    )
    fun observeSavingsCents(userId: String, from: Long, to: Long): Flow<Long>
}
