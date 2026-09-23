package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.repository.AlertRepository
import mx.equilibrio.domain.repository.TransactionRepository
import mx.equilibrio.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

/**
 * No existe (todavía) un "índice de equilibrio" propio en el dominio. Como
 * proxy honesto del equilibrio esencial/recreativo que da nombre al producto,
 * esta alerta compara la proporción de gasto recreativo contra un objetivo
 * 50/50; si se desvía más de [DEVIATION_THRESHOLD], dispara la alerta. Ajustar
 * aquí si Producto define un indicador propio más adelante.
 */
class CheckBalanceDeviationAlertUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val alertRepository: AlertRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke() {
        userRepository.getCurrentUser() ?: return
        if (alertRepository.hasActiveAlertOfType(AlertType.BALANCE_DEVIATION)) return

        val transactions = transactionRepository.observeAll().first()
        val essentialCents = transactions
            .filter { it.classification == Classification.ESSENTIAL }
            .sumOf { it.amountCents }
        val recreationalCents = transactions
            .filter { it.classification == Classification.RECREATIONAL }
            .sumOf { it.amountCents }

        val totalExpenseCents = essentialCents + recreationalCents
        if (totalExpenseCents <= 0) return

        val recreationalRatio = recreationalCents.toDouble() / totalExpenseCents
        val deviation = abs(recreationalRatio - TARGET_RECREATIONAL_RATIO)
        if (deviation <= DEVIATION_THRESHOLD) return

        alertRepository.insert(
            Alert(
                id = UUID.randomUUID().toString(),
                userId = userRepository.getCurrentUser()!!.id,
                type = AlertType.BALANCE_DEVIATION,
                referenceId = null,
                isRead = false,
                triggeredAt = System.currentTimeMillis(),
            ),
        )
    }

    companion object {
        private const val TARGET_RECREATIONAL_RATIO = 0.5
        private const val DEVIATION_THRESHOLD = 0.20
    }
}
