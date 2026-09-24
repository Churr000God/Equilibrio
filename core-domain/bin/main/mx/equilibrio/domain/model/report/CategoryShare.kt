package mx.equilibrio.domain.model.report

/** Gasto de una categoría y su proporción sobre el gasto total del periodo. `categoryId` null = sin categoría. */
data class CategoryShare(
    val categoryId: String?,
    val name: String?,
    val cents: Long,
    val share: Float,
)
