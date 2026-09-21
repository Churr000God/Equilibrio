package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.AlertRepository
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

/** Dispara AlertType.CARD_DUE cuando algún periodo de tarjeta entra en AWAITING_PAYMENT. */
class CheckCardDueAlertUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val periodRepository: PeriodRepository,
    private val settleDuePeriods: SettleDuePeriods,
    private val alertRepository: AlertRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(today: LocalDate) {
        val user = userRepository.getCurrentUser() ?: return
        if (alertRepository.hasActiveAlertOfType(AlertType.CARD_DUE)) return

        val cardAccounts = accountRepository.observeAll().first().filter { it.type == AccountType.CREDIT_CARD }
        cardAccounts.forEach { settleDuePeriods(it.id, today) }

        val duePeriod = cardAccounts
            .flatMap { periodRepository.getActiveByAccount(it.id) }
            .firstOrNull { it.state == PeriodState.AWAITING_PAYMENT }
            ?: return

        alertRepository.insert(
            Alert(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                type = AlertType.CARD_DUE,
                referenceId = duePeriod.id,
                isRead = false,
                triggeredAt = System.currentTimeMillis(),
            ),
        )
    }
}
