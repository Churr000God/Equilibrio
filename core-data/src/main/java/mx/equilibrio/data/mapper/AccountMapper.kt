package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    userId = userId,
    name = name,
    type = AccountType.valueOf(type),
    balanceCents = balanceCents,
    creditLimitCents = creditLimitCents,
    statementDay = statementDay,
    dueDay = dueDay,
    colorSlot = colorSlot,
    lastDigits = lastDigits,
)

fun Account.toEntity(syncState: String, updatedAt: Long, isDeleted: Boolean = false): AccountEntity = AccountEntity(
    id = id,
    userId = userId,
    name = name,
    type = type.name,
    balanceCents = balanceCents,
    creditLimitCents = creditLimitCents,
    statementDay = statementDay,
    dueDay = dueDay,
    colorSlot = colorSlot,
    lastDigits = lastDigits,
    updatedAt = updatedAt,
    syncState = syncState,
    isDeleted = isDeleted,
)
