package mx.equilibrio.feature.entry

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.TransactionKind

sealed interface QuickEntryEvent {
    data class KindChanged(val kind: TransactionKind) : QuickEntryEvent
    data class AmountChanged(val raw: String) : QuickEntryEvent
    data class ClassificationChanged(val classification: Classification) : QuickEntryEvent
    data class DateChanged(val date: LocalDate) : QuickEntryEvent
    data class NoteChanged(val text: String) : QuickEntryEvent
    data object SaveClicked : QuickEntryEvent
}
