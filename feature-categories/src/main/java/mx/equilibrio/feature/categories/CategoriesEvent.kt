package mx.equilibrio.feature.categories

sealed interface CategoriesEvent {
    data object PreviousMonth : CategoriesEvent
    data object NextMonth : CategoriesEvent
}
