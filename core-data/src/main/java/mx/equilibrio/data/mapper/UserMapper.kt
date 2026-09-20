package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.UserEntity
import mx.equilibrio.domain.model.PlanTier
import mx.equilibrio.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    googleId = googleId,
    email = email,
    displayName = displayName,
    givenName = givenName,
    familyName = familyName,
    photoUrl = photoUrl,
    plan = PlanTier.valueOf(plan),
    hasPassword = passwordHash != null,
)
