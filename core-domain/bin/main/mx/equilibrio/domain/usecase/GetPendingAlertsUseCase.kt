package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.repository.AlertRepository
import javax.inject.Inject

class GetPendingAlertsUseCase @Inject constructor(
    private val repository: AlertRepository,
) {
    operator fun invoke(): Flow<List<Alert>> = repository.getPendingAlerts()
}
