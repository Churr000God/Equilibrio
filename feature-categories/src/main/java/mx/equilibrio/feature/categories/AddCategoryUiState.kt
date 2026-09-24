package mx.equilibrio.feature.categories

import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.ui.components.toCentsOrZero

/** Slots de color fijos (§categoryColor en CategoryRow.kt) — no hay más que estos 3. */
const val CategoryColorSlotCount = 3

data class AddCategoryUiState(
    val type: CategoryType = CategoryType.EXPENSE,
    val name: String = "",
    val colorSlot: Int = 0,
    val icon: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val isEditing: Boolean = false,
    val isSystem: Boolean = false,
    val movementsCount: Int = 0,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
    val budgetEnabled: Boolean = false,
    val budgetInput: String = "",
) {
    val canSave: Boolean
        get() = !isSaving && validate() == null

    /** null = sin presupuesto asignado; el toggle desactivado nunca manda un monto aunque quede texto en el input. */
    val budgetCents: Long? get() = if (budgetEnabled) budgetInput.toCentsOrZero() else null
}

/** Devuelve el estado con los mensajes de error llenos, o null si todo pasa. */
fun AddCategoryUiState.validate(): AddCategoryUiState? {
    val nameError = if (name.isBlank()) "Ponle un nombre a la categoría." else null
    if (nameError == null) return null
    return copy(nameError = nameError)
}
