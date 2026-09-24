package mx.equilibrio.data.mapper

import mx.equilibrio.data.local.entity.AlertEntity
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType

fun AlertEntity.toDomain(): Alert = Alert(
    id = id,
    userId = userId,
    type = AlertType.valueOf(type),
    referenceId = referenceId,
    // Room/SQLite no tiene tipo boolean nativo; isRead se persiste como 0/1.
    isRead = isRead != 0,
    triggeredAt = triggeredAt,
)
