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
    // hasPassword no se persiste: se deriva de si existe un hash guardado, para que la UI
    // pueda decidir si ofrece "iniciar sesión con contraseña" sin exponer el hash al dominio.
    hasPassword = passwordHash != null,
)
