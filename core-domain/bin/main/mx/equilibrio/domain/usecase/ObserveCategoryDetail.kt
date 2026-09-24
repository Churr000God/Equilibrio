package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryDetail
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.repository.CategoryRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Inject

class ObserveCategoryDetail @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) {
    /** [today] lo resuelve el caller (convención del módulo: dominio no lee el reloj). */
    operator fun invoke(categoryId: String, today: LocalDate): Flow<CategoryDetail> {
        val month = YearMonth.of(today)
        return combine(
            categoryFlow(categoryId),
            transactionRepository.observeByCategory(categoryId),
        ) { category, transactions ->
            val monthTransactions = transactions
                .filter { it.occurredAt in month }
                .sortedByDescending { it.occurredAt }
            val monthTotalCents = monthTransactions.sumOf { it.amountCents }
            val movementsCount = monthTransactions.size
            CategoryDetail(
                category = category,
                monthTotalCents = monthTotalCents,
                movementsCount = movementsCount,
                averageCents = if (movementsCount == 0) 0L else monthTotalCents / movementsCount,
                recentTransactions = monthTransactions.take(RECENT_LIMIT),
            )
        }
    }

    private fun categoryFlow(categoryId: String): Flow<Category> =
        categoryRepository.observeAll()
            .map { categories -> categories.find { it.id == categoryId } }
            .filterNotNull()

    companion object {
        private const val RECENT_LIMIT = 10
    }
}
