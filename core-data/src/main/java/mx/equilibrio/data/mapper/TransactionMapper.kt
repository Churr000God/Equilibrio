package mx.equilibrio.data.mapper

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus

// Convención compartida por todos los mappers de core-data: las fechas se persisten como
// epoch millis en UTC (inicio de día), nunca con la zona horaria del dispositivo, para que
// dos dispositivos con distinto huso no desincronicen la fecha de un mismo movimiento.
fun LocalDate.toEpochMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

fun Long.toLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    userId = userId,
    accountId = accountId,
    kind = TransactionKind.valueOf(kind),
    classification = classification?.let { Classification.valueOf(it) },
    amountCents = amountCents,
    occurredAt = occurredAt.toLocalDate(),
    note = note,
    categoryId = categoryId,
    status = TransactionStatus.valueOf(status),
    periodId = periodId,
    goalId = goalId,
    installmentPlanId = installmentPlanId,
    installmentIndex = installmentIndex,
    installmentCount = installmentCount,
    recurringSeriesId = recurringSeriesId,
)

fun Transaction.toEntity(syncState: String, updatedAt: Long, isDeleted: Boolean = false): TransactionEntity = TransactionEntity(
    id = id,
    userId = userId,
    accountId = accountId,
    kind = kind.name,
    classification = classification?.name,
    amountCents = amountCents,
    occurredAt = occurredAt.toEpochMillis(),
    note = note,
    categoryId = categoryId,
    updatedAt = updatedAt,
    syncState = syncState,
    isDeleted = isDeleted,
    status = status.name,
    periodId = periodId,
    goalId = goalId,
    installmentPlanId = installmentPlanId,
    installmentIndex = installmentIndex,
    installmentCount = installmentCount,
    recurringSeriesId = recurringSeriesId,
)
