package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.dao.GoalWithSaved
import mx.equilibrio.data.local.entity.GoalEntity
import mx.equilibrio.domain.model.Goal
import mx.equilibrio.domain.model.GoalStatus

fun GoalWithSaved.toDomain(): Goal = Goal(
    id = goal.id,
    userId = goal.userId,
    name = goal.name,
    targetCents = goal.targetCents,
    savedCents = savedCents,
    deadline = goal.deadline?.toLocalDate(),
    status = GoalStatus.valueOf(goal.status),
    createdAt = goal.createdAt.toLocalDate(),
)

fun Goal.toEntity(syncState: String, updatedAt: Long, isDeleted: Boolean = false): GoalEntity = GoalEntity(
    id = id,
    userId = userId,
    name = name,
    targetCents = targetCents,
    deadline = deadline?.toEpochMillis(),
    status = status.name,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt,
    syncState = syncState,
    isDeleted = isDeleted,
)
