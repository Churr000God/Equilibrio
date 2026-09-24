package mx.equilibrio.domain.model

/** Detalle de una categoría para el mes calendario actual. */
data class CategoryDetail(
    val category: Category,
    val monthTotalCents: Long,
    val movementsCount: Int,
    val averageCents: Long,
    val recentTransactions: List<Transaction>,
)
