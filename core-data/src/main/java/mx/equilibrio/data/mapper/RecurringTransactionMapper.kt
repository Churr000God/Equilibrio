package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.RecurringTransactionEntity
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.model.TransactionKind

fun RecurringTransactionEntity.toDomain(): RecurringTransaction = RecurringTransaction(
    id = id,
    userId = userId,
    accountId = accountId,
    kind = TransactionKind.valueOf(kind),
    classification = Classification.valueOf(classification),
    amountCents = amountCents,
    note = note,
    categoryId = categoryId,
    frequency = RecurrenceFrequency.valueOf(frequency),
    anchorDay = anchorDay,
    nextOccurrenceAt = nextOccurrenceAt.toLocalDate(),
    isActive = isActive,
    createdAt = createdAt.toLocalDate(),
)

fun RecurringTransaction.toEntity(
    syncState: String,
    updatedAt: Long,
    isDeleted: Boolean = false,
): RecurringTransactionEntity = RecurringTransactionEntity(
    id = id,
    userId = userId,
    accountId = accountId,
    kind = kind.name,
    classification = classification.name,
    amountCents = amountCents,
    note = note,
    categoryId = categoryId,
    frequency = frequency.name,
    anchorDay = anchorDay,
    nextOccurrenceAt = nextOccurrenceAt.toEpochMillis(),
    isActive = isActive,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt,
    syncState = syncState,
    isDeleted = isDeleted,
)
