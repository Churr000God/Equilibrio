package mx.equilibrio.feature.entry

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
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.usecase.GetTransaction
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.SaveTransaction
import java.util.UUID
import javax.inject.Inject

fun today() = Clock.System.todayIn(TimeZone.UTC)

@HiltViewModel
class QuickEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAccounts: ObserveAccounts,
    private val getTransaction: GetTransaction,
    private val saveTransaction: SaveTransaction,
) : ViewModel() {

    private val editingId: String? = savedStateHandle["transactionId"]

    private val _state = MutableStateFlow(QuickEntryUiState(occurredAt = today()))
    val state: StateFlow<QuickEntryUiState> = _state.asStateFlow()

    private var userId: String? = null

    init {
        viewModelScope.launch {
            val accounts = observeAccounts().first()
            val defaultAccountId = accounts.firstOrNull()?.id
            userId = accounts.firstOrNull()?.userId

            val existing = editingId?.let { getTransaction(it) }
            if (existing != null) {
                userId = existing.userId
                _state.update {
                    it.copy(
                        kind = existing.kind,
                        amountInput = formatAmountInput(existing.amountCents),
                        classification = existing.classification,
                        accountId = existing.accountId,
                        occurredAt = existing.occurredAt,
                        note = existing.note.orEmpty(),
                        isEditing = true,
                    )
                }
            } else {
                _state.update { it.copy(accountId = defaultAccountId) }
            }
        }
    }

    fun onEvent(event: QuickEntryEvent) {
        when (event) {
            is QuickEntryEvent.KindChanged -> _state.update {
                it.copy(kind = event.kind, classification = null)
            }

            is QuickEntryEvent.AmountChanged -> _state.update {
                it.copy(amountInput = sanitizeAmountInput(event.raw), amountError = null)
            }

            is QuickEntryEvent.ClassificationChanged -> _state.update {
                it.copy(classification = event.classification)
            }

            is QuickEntryEvent.DateChanged -> _state.update {
                it.copy(
                    occurredAt = event.date,
                    dateError = if (event.date > today()) "Elige una fecha de hoy o anterior." else null,
                )
            }

            is QuickEntryEvent.NoteChanged -> _state.update { it.copy(note = event.text) }

            QuickEntryEvent.SaveClicked -> save()
        }
    }

    private fun save() {
        val current = _state.value
        if (current.amountCents <= 0) {
            _state.update { it.copy(amountError = "Agrega un monto para guardar.") }
            return
        }
        if (!current.canSave) return

        val accountId = current.accountId ?: return
        val classification = current.classification ?: return
        val ownerId = userId ?: return

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            saveTransaction(
                Transaction(
                    id = editingId ?: UUID.randomUUID().toString(),
                    userId = ownerId,
                    accountId = accountId,
                    kind = current.kind,
                    classification = classification,
                    amountCents = current.amountCents,
                    occurredAt = current.occurredAt,
                    note = current.note.ifBlank { null },
                ),
            )
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}

private fun sanitizeAmountInput(raw: String): String {
    val filtered = raw.filterIndexed { index, c -> c.isDigit() || (c == '.' && !raw.take(index).contains('.')) }
    val parts = filtered.split(".")
    return if (parts.size > 1) "${parts[0]}.${parts[1].take(2)}" else filtered
}

private fun formatAmountInput(cents: Long): String {
    val whole = cents / 100
    val fraction = cents % 100
    return if (fraction == 0L) whole.toString() else "$whole.${fraction.toString().padStart(2, '0')}"
}
