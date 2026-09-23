package mx.equilibrio.feature.categories

import mx.equilibrio.domain.model.CategoryType

sealed interface AddCategoryEvent {
    data class TypeChanged(val type: CategoryType) : AddCategoryEvent
    data class NameChanged(val text: String) : AddCategoryEvent
    data class ColorSlotChanged(val slot: Int) : AddCategoryEvent
    data class IconChanged(val icon: String) : AddCategoryEvent
    data class BudgetToggled(val enabled: Boolean) : AddCategoryEvent
    data class BudgetAmountChanged(val raw: String) : AddCategoryEvent
    data object SaveClicked : AddCategoryEvent
    /** [reassignToOtros] false = borra los movimientos en cascada. */
    data class DeleteConfirmed(val reassignToOtros: Boolean) : AddCategoryEvent
}
