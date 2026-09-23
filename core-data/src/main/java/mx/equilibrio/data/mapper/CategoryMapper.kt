package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.CategoryEntity
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    userId = userId,
    name = name,
    type = CategoryType.valueOf(type),
    colorSlot = colorSlot,
    icon = icon,
    isSystem = isSystem,
    sortOrder = sortOrder,
    monthlyBudgetCents = monthlyBudgetCents,
)

fun Category.toEntity(syncState: String, updatedAt: Long, isDeleted: Boolean = false): CategoryEntity = CategoryEntity(
    id = id,
    userId = userId,
    name = name,
    type = type.name,
    colorSlot = colorSlot,
    icon = icon,
    isSystem = isSystem,
    sortOrder = sortOrder,
    updatedAt = updatedAt,
    syncState = syncState,
    isDeleted = isDeleted,
    monthlyBudgetCents = monthlyBudgetCents,
)
