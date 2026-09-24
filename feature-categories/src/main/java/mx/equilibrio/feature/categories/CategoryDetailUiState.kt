package mx.equilibrio.feature.categories

import mx.equilibrio.domain.model.CategoryType

data class RecentTransactionUi(
    val id: String,
    val note: String,
    val dateLabel: String,
    val accountLabel: String,
    val amountCents: Long,
)

data class CategoryDetailUiState(
    val isLoading: Boolean = true,
    val id: String = "",
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val colorSlot: Int = 0,
    val icon: String = "",
    val isSystem: Boolean = false,
    val monthTotalCents: Long = 0,
    val movementsCount: Int = 0,
    val averageCents: Long = 0,
    val monthlyBudgetCents: Long? = null,
    val recentTransactions: List<RecentTransactionUi> = emptyList(),
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
) {
    val typeLabel: String
        get() = when (type) {
            CategoryType.INCOME -> "Ingreso"
            CategoryType.EXPENSE -> "Gasto"
        }

    // Se topa en 1f aunque el gasto supere el presupuesto: la barra de progreso no debe desbordarse visualmente.
    val budgetProgress: Float?
        get() = monthlyBudgetCents?.let { budget ->
            if (budget <= 0) 0f else (monthTotalCents.toFloat() / budget.toFloat()).coerceIn(0f, 1f)
        }

    // Puede salir negativo: significa que ya se pasó del presupuesto ese monto (ver "Te pasaste..." en la UI).
    val budgetRemainingCents: Long?
        get() = monthlyBudgetCents?.let { it - monthTotalCents }
}

sealed interface CategoryDetailEvent {
    /** [reassignToOtros] false = borra los movimientos en cascada. */
    data class DeleteConfirmed(val reassignToOtros: Boolean) : CategoryDetailEvent
}
