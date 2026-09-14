package mx.equilibrio.feature.categories

import mx.equilibrio.domain.model.CategoryType

sealed interface AddCategoryEvent {
    data class TypeChanged(val type: CategoryType) : AddCategoryEvent
    data class NameChanged(val text: String) : AddCategoryEvent
    data class ColorSlotChanged(val slot: Int) : AddCategoryEvent
    data object SaveClicked : AddCategoryEvent
}
