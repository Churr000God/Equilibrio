package mx.equilibrio.feature.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.usecase.DeleteCategory
import mx.equilibrio.domain.usecase.EnsureOtrosCategory
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveCategoryDetail
import mx.equilibrio.domain.usecase.SaveCategory
import mx.equilibrio.ui.components.formatAmountInput
import mx.equilibrio.ui.components.sanitizeAmountInput
import java.util.UUID
import javax.inject.Inject

/**
 * Formulario de alta/edición de categoría. A diferencia de un ViewModel de simple mapeo,
 * también resuelve el borrado (con reasignación opcional de movimientos a "Otros", ver
 * [CategoryDeleteDialog]) y precarga los datos existentes cuando [editingId] viene en el
 * SavedStateHandle.
 */
@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAccounts: ObserveAccounts,
    private val getCategories: GetCategories,
    private val saveCategory: SaveCategory,
    private val deleteCategory: DeleteCategory,
    private val ensureOtrosCategory: EnsureOtrosCategory,
    private val observeCategoryDetail: ObserveCategoryDetail,
) : ViewModel() {

    private val editingId: String? = savedStateHandle["categoryId"]

    private val _state = MutableStateFlow(AddCategoryUiState())
    val state: StateFlow<AddCategoryUiState> = _state.asStateFlow()

    private var existing: Category? = null

    init {
        editingId?.let { id ->
            viewModelScope.launch {
                val category = getCategories().first().find { it.id == id } ?: return@launch
                existing = category
                _state.update {
                    it.copy(
                        type = category.type,
                        name = category.name,
                        colorSlot = category.colorSlot,
                        icon = category.icon,
                        isEditing = true,
                        isSystem = category.isSystem,
                        budgetEnabled = category.monthlyBudgetCents != null,
                        budgetInput = category.monthlyBudgetCents?.let { cents -> formatAmountInput(cents) } ?: "",
                    )
                }
            }
            viewModelScope.launch {
                val detail = observeCategoryDetail(id, today()).first()
                _state.update { it.copy(movementsCount = detail.movementsCount) }
            }
        }
    }

    fun onEvent(event: AddCategoryEvent) {
        when (event) {
            is AddCategoryEvent.TypeChanged -> _state.update { it.copy(type = event.type) }
            is AddCategoryEvent.NameChanged -> _state.update { it.copy(name = event.text, nameError = null) }
            is AddCategoryEvent.ColorSlotChanged -> _state.update { it.copy(colorSlot = event.slot) }
            is AddCategoryEvent.IconChanged -> _state.update { it.copy(icon = event.icon) }
            is AddCategoryEvent.BudgetToggled -> _state.update { it.copy(budgetEnabled = event.enabled) }
            is AddCategoryEvent.BudgetAmountChanged -> _state.update { it.copy(budgetInput = sanitizeAmountInput(event.raw)) }
            AddCategoryEvent.SaveClicked -> save()
            is AddCategoryEvent.DeleteConfirmed -> delete(event.reassignToOtros)
        }
    }

    private fun save() {
        val current = _state.value
        val invalid = current.validate()
        if (invalid != null) {
            _state.update { invalid }
            return
        }

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val base = existing
            /** No hay login todavía: el userId sale de la cuenta "Efectivo" que LocalSession siembra siempre. */
            val ownerId = base?.userId ?: observeAccounts().first().firstOrNull()?.userId
            if (ownerId == null) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            // Categoría nueva: se agrega al final del orden actual; si ya existía, conserva su posición.
            val nextSortOrder = base?.sortOrder ?: getCategories().first().size

            saveCategory(
                Category(
                    id = base?.id ?: UUID.randomUUID().toString(),
                    userId = ownerId,
                    name = current.name.trim(),
                    type = current.type,
                    colorSlot = current.colorSlot,
                    icon = current.icon,
                    isSystem = base?.isSystem ?: false,
                    sortOrder = nextSortOrder,
                    monthlyBudgetCents = current.budgetCents,
                ),
            )
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }

    private fun delete(reassignToOtros: Boolean) {
        val base = existing ?: return
        _state.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            val targetId = if (reassignToOtros) ensureOtrosCategory(base.userId, base.type).id else null
            deleteCategory(base.id, targetId)
            _state.update { it.copy(isDeleting = false, deleted = true) }
        }
    }
}
