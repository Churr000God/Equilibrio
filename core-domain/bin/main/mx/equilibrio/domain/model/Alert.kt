package mx.equilibrio.domain.model

data class Alert(
    val id: String,
    val userId: String,
    val type: AlertType,
    // A qué entidad apunta la alerta depende de `type`: cuenta para BALANCE_DEVIATION/CARD_DUE, meta para GOAL_AT_RISK.
    val referenceId: String?,
    val isRead: Boolean,
    val triggeredAt: Long,
)
