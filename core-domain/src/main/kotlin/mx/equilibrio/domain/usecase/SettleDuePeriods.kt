package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * No hay infraestructura de background jobs en el proyecto: las transiciones de
 * estado se resuelven de forma perezosa acá, invocado al leer/observar periodos
 * de una cuenta. Procesa el periodo activo más antiguo repetidamente (no una
 * sola pasada) para poder ponerse al día de una sola llamada aunque hayan
 * quedado varios ciclos vencidos sin abrir la app (ej. AWAITING_PAYMENT que ya
 * venció payAt, y el periodo siguiente que a su vez también ya venció).
 */
class SettleDuePeriods @Inject constructor(
    private val periodRepository: PeriodRepository,
    private val transactionRepository: TransactionRepository,
    private val getOrCreatePeriodForDate: GetOrCreatePeriodForDate,
) {
    suspend operator fun invoke(accountId: String, today: LocalDate) {
        while (true) {
            val target = periodRepository.observeByAccount(accountId).first()
                .filter { it.state != PeriodState.CLOSED }
                .minByOrNull { it.startAt }
                ?: return

            when (target.state) {
                PeriodState.OPEN -> {
                    if (today <= target.endAt) return
                    periodRepository.upsert(target.copy(state = PeriodState.AWAITING_PAYMENT))
                }

                PeriodState.AWAITING_PAYMENT -> {
                    if (today <= target.payAt) return

                    val spent = transactionRepository.observeAll().first()
                        .filter { it.periodId == target.id && it.kind == TransactionKind.EXPENSE }
                        .sumOf { it.amountCents }
                    val finalBalance = target.carriedBalanceCents + spent - target.amountPaidCents

                    periodRepository.upsert(target.copy(state = PeriodState.CLOSED))

                    val next = getOrCreatePeriodForDate(accountId, target.endAt.plus(1, DateTimeUnit.DAY))
                    periodRepository.upsert(next.copy(carriedBalanceCents = finalBalance))
                }

                PeriodState.CLOSED -> return
            }
        }
    }
}
