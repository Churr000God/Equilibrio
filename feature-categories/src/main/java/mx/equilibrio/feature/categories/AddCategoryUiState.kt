package mx.equilibrio.feature.categories

import mx.equilibrio.domain.model.CategoryType

/** Slots de color fijos (§categoryColor en CategoryRow.kt) — no hay más que estos 3. */
const val CategoryColorSlotCount = 3

data class AddCategoryUiState(
    val type: CategoryType = CategoryType.EXPENSE,
    val name: String = "",
    val colorSlot: Int = 0,
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving && validate() == null
}

/** Devuelve el estado con los mensajes de error llenos, o null si todo pasa. */
fun AddCategoryUiState.validate(): AddCategoryUiState? {
    val nameError = if (name.isBlank()) "Ponle un nombre a la categoría." else null
    if (nameError == null) return null
    return copy(nameError = nameError)
}
