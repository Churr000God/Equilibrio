package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType

interface AlertRepository {
    fun getPendingAlerts(): Flow<List<Alert>>
    suspend fun hasActiveAlertOfType(type: AlertType): Boolean
    suspend fun insert(alert: Alert)
    suspend fun markAsRead(alertId: String)
    suspend fun softDelete(alertId: String)
}
