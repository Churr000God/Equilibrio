package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.repository.PeriodRepository
import javax.inject.Inject

/**
 * Para la UI de detalle de tarjeta. No corre [SettleDuePeriods] por sí mismo
 * (mantiene la firma como fuente reactiva pura, sin depender de un `today`
 * implícito) — el caller debe invocar `SettleDuePeriods(accountId, today)`
 * antes de suscribirse si quiere ver las transiciones de estado al día.
 */
class ObservePeriods @Inject constructor(
    private val periodRepository: PeriodRepository,
) {
    operator fun invoke(accountId: String): Flow<List<Period>> = periodRepository.observeByAccount(accountId)
}
