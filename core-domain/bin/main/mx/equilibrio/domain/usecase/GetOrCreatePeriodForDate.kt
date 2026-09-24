package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.model.PeriodBounds
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.domain.model.deriveCycleBounds
import mx.equilibrio.domain.model.nextCycleBounds
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.PeriodRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Busca el periodo de [accountId] que contiene [date]; si no existe, encadena
 * secuencialmente desde el último periodo conocido de la cuenta (o desde
 * `statementDay`/`dueDay` de la cuenta si es el primero) creando los periodos
 * intermedios que hagan falta. Cada uno arranca con `carriedBalanceCents = 0`
 * como placeholder — se corrige en cascada cuando [SettleDuePeriods] cierre el
 * periodo previo real. Resuelve así el caso de una compra programada varios
 * ciclos adelante.
 */
class GetOrCreatePeriodForDate @Inject constructor(
    private val periodRepository: PeriodRepository,
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: String, date: LocalDate): Period {
        val account = accountRepository.getById(accountId)
        requireNotNull(account) { "No existe la cuenta $accountId" }
        val statementDay = requireNotNull(account.statementDay) { "La cuenta $accountId no tiene statementDay configurado" }
        val dueDay = requireNotNull(account.dueDay) { "La cuenta $accountId no tiene dueDay configurado" }

        val periods = periodRepository.observeByAccount(accountId).first()
        periods.firstOrNull { date >= it.startAt && date <= it.endAt }?.let { return it }

        val latest = periods.maxByOrNull { it.endAt }
        if (latest == null || date < latest.startAt) {
            // Sin periodos previos, o fecha anterior a cualquiera conocido (caso raro de
            // compra retroactiva): no hay de qué encadenar, se crea el ciclo aislado que
            // contiene `date` directamente.
            return createPeriod(accountId, deriveCycleBounds(statementDay, dueDay, date))
        }

        var bounds = PeriodBounds(latest.startAt, latest.endAt, latest.payAt, statementDay, dueDay)
        var current = latest
        while (date > bounds.endAt) {
            bounds = nextCycleBounds(bounds)
            current = createPeriod(accountId, bounds)
        }
        return current
    }

    private suspend fun createPeriod(accountId: String, bounds: PeriodBounds): Period {
        val period = Period(
            id = UUID.randomUUID().toString(),
            accountId = accountId,
            startAt = bounds.startAt,
            endAt = bounds.endAt,
            payAt = bounds.payAt,
            state = PeriodState.OPEN,
            carriedBalanceCents = 0,
            amountPaidCents = 0,
        )
        periodRepository.upsert(period)
        return period
    }
}
