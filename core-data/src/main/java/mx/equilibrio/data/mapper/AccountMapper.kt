package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    userId = userId,
    name = name,
    type = AccountType.valueOf(type),
)
