package mx.equilibrio.feature.categories

import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.domain.model.YearMonth

data class CategoryUi(
    val id: String,
    val name: String,
    val type: CategoryType,
    val colorSlot: Int = 0,
    val icon: String,
    /** Suma del mes en curso, ya filtrada a movimientos completados. */
    val amountCents: Long = 0,
    val movementsCount: Int = 0,
    /** Proporción sobre el total del tipo (0f..1f) en el mes en curso. */
    val share: Float = 0f,
){
    val typeLabel: String
        get() = when(type){
            CategoryType.INCOME -> "Ingreso"
            CategoryType.EXPENSE -> "Gasto"
        }
}

data class CategoriesUiState(
    val month: YearMonth,
    val isLoading: Boolean = true,
    val categories: List<CategoryUi> = emptyList(),
    val expenseTotalCents: Long = 0,
    val incomeTotalCents: Long = 0,
    val canGoForward: Boolean = false,
){
    val isEmpty: Boolean get() = !isLoading && categories.isEmpty()
    val expenseCategories: List<CategoryUi> get() = categories.filter { it.type == CategoryType.EXPENSE }
    val incomeCategories: List<CategoryUi> get() = categories.filter { it.type == CategoryType.INCOME }
}
