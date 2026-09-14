package mx.equilibrio.feature.categories

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
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.SaveCategory
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val observeAccounts: ObserveAccounts,
    private val getCategories: GetCategories,
    private val saveCategory: SaveCategory,
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryUiState())
    val state: StateFlow<AddCategoryUiState> = _state.asStateFlow()

    fun onEvent(event: AddCategoryEvent) {
        when (event) {
            is AddCategoryEvent.TypeChanged -> _state.update { it.copy(type = event.type) }
            is AddCategoryEvent.NameChanged -> _state.update { it.copy(name = event.text, nameError = null) }
            is AddCategoryEvent.ColorSlotChanged -> _state.update { it.copy(colorSlot = event.slot) }
            AddCategoryEvent.SaveClicked -> save()
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
            /** No hay login todavía: el userId sale de la cuenta "Efectivo" que LocalSession siembra siempre. */
            val ownerId = observeAccounts().first().firstOrNull()?.userId
            if (ownerId == null) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            val nextSortOrder = getCategories().first().size

            saveCategory(
                Category(
                    id = UUID.randomUUID().toString(),
                    userId = ownerId,
                    name = current.name.trim(),
                    type = current.type,
                    colorSlot = current.colorSlot,
                    icon = "",
                    sortOrder = nextSortOrder,
                ),
            )
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
