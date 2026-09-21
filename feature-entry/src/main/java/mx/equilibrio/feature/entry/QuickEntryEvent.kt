package mx.equilibrio.feature.entry

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification

sealed interface QuickEntryEvent {
    data class EntryModeChanged(val mode: EntryMode) : QuickEntryEvent
    data class AmountChanged(val raw: String) : QuickEntryEvent
    data class ClassificationChanged(val classification: Classification) : QuickEntryEvent
    data class DateChanged(val date: LocalDate) : QuickEntryEvent
    data class NoteChanged(val text: String) : QuickEntryEvent
    data class CategorySelected(val categoryId: String?) : QuickEntryEvent
    data class AccountSelected(val accountId: String) : QuickEntryEvent
    data class CreateCategoryTabToggled(val show: Boolean) : QuickEntryEvent
    data class NewCategoryNameChanged(val text: String) : QuickEntryEvent
    data class NewCategoryColorSlotChanged(val slot: Int) : QuickEntryEvent
    data object SaveCategoryClicked : QuickEntryEvent
    data class OriginAccountSelected(val accountId: String) : QuickEntryEvent
    data class DestinationAccountSelected(val accountId: String) : QuickEntryEvent
    data class CreditAccountSelected(val accountId: String) : QuickEntryEvent
    data class ViewTabChanged(val tab: EntryViewTab) : QuickEntryEvent
    data object ConfirmClicked : QuickEntryEvent
    data object SaveClicked : QuickEntryEvent
}
