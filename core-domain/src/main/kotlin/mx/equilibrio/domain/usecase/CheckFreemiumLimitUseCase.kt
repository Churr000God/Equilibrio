package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.FreemiumResult
import mx.equilibrio.domain.model.PlanTier
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class CheckFreemiumLimitUseCase @Inject constructor(
    private val getAccountCount: GetAccountCountUseCase,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): FreemiumResult {
        val plan = userRepository.getCurrentUser()?.plan ?: PlanTier.FREE
        val reachedLimit = plan == PlanTier.FREE && getAccountCount() >= FREE_ACCOUNT_LIMIT
        return if (reachedLimit) FreemiumResult.LIMIT_REACHED else FreemiumResult.OK
    }

    companion object {
        const val FREE_ACCOUNT_LIMIT = 2
    }
}
