package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.PeriodEntity
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.model.PeriodState

fun PeriodEntity.toDomain(): Period = Period(
    id = id,
    accountId = accountId,
    startAt = startAt.toLocalDate(),
    endAt = endAt.toLocalDate(),
    payAt = payAt.toLocalDate(),
    state = PeriodState.valueOf(state),
    carriedBalanceCents = carriedBalanceCents,
    amountPaidCents = amountPaidCents,
)

fun Period.toEntity(syncState: String, updatedAt: Long, createdAt: Long, isDeleted: Boolean = false): PeriodEntity = PeriodEntity(
    id = id,
    accountId = accountId,
    startAt = startAt.toEpochMillis(),
    endAt = endAt.toEpochMillis(),
    payAt = payAt.toEpochMillis(),
    state = state.name,
    carriedBalanceCents = carriedBalanceCents,
    amountPaidCents = amountPaidCents,
    updatedAt = updatedAt,
    syncState = syncState,
    isDeleted = isDeleted,
    createdAt = createdAt,
)
