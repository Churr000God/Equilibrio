package mx.equilibrio.domain.model

data class Alert(
    val id: String,
    val userId: String,
    val type: AlertType,
    val referenceId: String?,
    val isRead: Boolean,
    val triggeredAt: Long,
)
