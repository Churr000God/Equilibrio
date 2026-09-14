package mx.equilibrio.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.usecase.GetCategories
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    getCategories: GetCategories,
) : ViewModel() {

    val state: StateFlow<CategoriesUiState> = getCategories()
        .map { categories ->
            CategoriesUiState(
                isLoading = false,
                categories = categories.sortedBy { it.sortOrder }.map { it.toUi() },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoriesUiState(isLoading = true),
        )

    private fun Category.toUi() = CategoryUi(
        id = id,
        name = name,
        type = type,
        colorSlot = colorSlot,
        icon = icon,
    )
}
