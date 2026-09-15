package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.AlertRepository
import javax.inject.Inject

class MarkAlertAsReadUseCase @Inject constructor(
    private val repository: AlertRepository,
) {
    suspend operator fun invoke(alertId: String) = repository.markAsRead(alertId)
}
