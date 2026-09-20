package mx.equilibrio.domain.model.report

import mx.equilibrio.domain.model.Category

/** Gasto de una categoría y su proporción sobre el gasto total del periodo. `null` = sin categoría. */
data class CategoryShare(
    val category: Category?,
    val cents: Long,
    val share: Float,
)
