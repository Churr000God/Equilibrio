package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.FreemiumResult
import mx.equilibrio.domain.model.PlanTier
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class CheckFreemiumLimitUseCase @Inject constructor(
    private val getAccountCount: GetAccountCountUseCase,
    private val userRepository: UserRepository,
) {
    /** Efectivo nunca consume cupo: una cuenta CASH siempre se puede crear. */
    suspend operator fun invoke(type: AccountType): FreemiumResult {
        if (type == AccountType.CASH) return FreemiumResult.OK
        val plan = userRepository.getCurrentUser()?.plan ?: PlanTier.FREE
        val reachedLimit = plan == PlanTier.FREE && getAccountCount() >= FREE_ACCOUNT_LIMIT
        return if (reachedLimit) FreemiumResult.LIMIT_REACHED else FreemiumResult.OK
    }

    companion object {
        const val FREE_ACCOUNT_LIMIT = 2
    }
}
