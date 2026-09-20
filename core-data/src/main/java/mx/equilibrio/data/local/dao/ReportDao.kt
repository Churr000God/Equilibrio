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
    @ColumnInfo(name = "category_key") val categoryKey: String?,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
)

/**
 * Consultas agregadas sobre `transactions`. SQLite agrega y devuelve pocas
 * filas; nunca se recorre el historial en Kotlin (RNF03). Los abonos a metas
 * (`category_key = 'savings'`) se excluyen de todo salvo `observeSavingsCents`.
 */
@Dao
interface ReportDao {

    @Query(
        """
        SELECT kind, classification, SUM(amount_cents) AS total_cents
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0
          AND occurred_at BETWEEN :from AND :to
          AND (category_key IS NULL OR category_key != 'savings')
        GROUP BY kind, classification
        """,
    )
    fun observePeriodTotals(userId: String, from: Long, to: Long): Flow<List<PeriodTotalRow>>

    @Query(
        """
        SELECT strftime('%Y-%m', occurred_at / 1000, 'unixepoch') AS month, kind, SUM(amount_cents) AS total_cents
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0
          AND occurred_at BETWEEN :from AND :to
          AND (category_key IS NULL OR category_key != 'savings')
        GROUP BY month, kind
        ORDER BY month ASC
        """,
    )
    fun observeMonthlyTrend(userId: String, from: Long, to: Long): Flow<List<MonthlyRow>>

    @Query(
        """
        SELECT category_key, SUM(amount_cents) AS total_cents
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0 AND kind = 'EXPENSE'
          AND occurred_at BETWEEN :from AND :to
          AND (category_key IS NULL OR category_key != 'savings')
        GROUP BY category_key
        ORDER BY total_cents DESC
        """,
    )
    fun observeCategoryTotals(userId: String, from: Long, to: Long): Flow<List<CategoryRow>>

    @Query(
        """
        SELECT COALESCE(SUM(amount_cents), 0)
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0
          AND occurred_at BETWEEN :from AND :to
          AND category_key = 'savings'
        """,
    )
    fun observeSavingsCents(userId: String, from: Long, to: Long): Flow<Long>
}
