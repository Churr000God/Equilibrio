package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Una compra a meses no es editable: la única operación posible sobre un plan
 * ya creado es borrarlo completo (soft-delete de todas sus cuotas).
 */
class DeleteInstallmentPlan @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(planId: String) = repository.markInstallmentPlanDeleted(planId)
}
