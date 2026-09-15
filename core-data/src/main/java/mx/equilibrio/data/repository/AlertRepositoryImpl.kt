package mx.equilibrio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.dao.AlertDao
import mx.equilibrio.data.local.entity.AlertEntity
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType
import mx.equilibrio.domain.repository.AlertRepository
import javax.inject.Inject

class AlertRepositoryImpl @Inject constructor(
    private val dao: AlertDao,
    private val session: LocalSession,
) : AlertRepository {

    override fun getPendingAlerts(): Flow<List<Alert>> = flow {
        emitAll(dao.getPendingAlertsByUser(session.currentUserId()).map { list -> list.map { it.toDomain() } })
    }

    override suspend fun hasActiveAlertOfType(type: AlertType): Boolean =
        dao.getActiveByType(session.currentUserId(), type.name) != null

    override suspend fun insert(alert: Alert) {
        val now = System.currentTimeMillis()
        dao.insert(
            AlertEntity(
                id = alert.id,
                userId = alert.userId,
                type = alert.type.name,
                referenceId = alert.referenceId,
                isRead = if (alert.isRead) 1 else 0,
                triggeredAt = alert.triggeredAt,
                updatedAt = now,
            ),
        )
    }

    override suspend fun markAsRead(alertId: String) {
        dao.markAsRead(alertId, now = System.currentTimeMillis())
    }

    override suspend fun softDelete(alertId: String) {
        dao.softDelete(alertId, now = System.currentTimeMillis())
    }
}
