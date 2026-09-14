package mx.equilibrio.feature.categories

import mx.equilibrio.domain.model.CategoryType

data class CategoryUi(
    val id: String,
    val name: String,
    val type: CategoryType,
    val colorSlot: Int = 0,
    val icon: String,
){
    val typeLabel: String
        get() = when(type){
            CategoryType.INCOME -> "Ingreso"
            CategoryType.EXPENSE -> "Gasto"
        }
}

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<CategoryUi> = emptyList(),
){
    val isEmpty: Boolean get() = !isLoading && categories.isEmpty()
}