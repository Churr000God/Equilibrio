package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Transaction

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeBalanceCents(): Flow<Long>
    suspend fun getById(id: String): Transaction?
    suspend fun upsert(transaction: Transaction)
    suspend fun delete(id: String)

    /** Soft-delete de todas las cuotas de una compra a meses. */
    suspend fun markInstallmentPlanDeleted(planId: String)

    fun observeByCategory(categoryId: String): Flow<List<Transaction>>

    /** Reasigna todos los movimientos de [fromCategoryId] a [toCategoryId]. */
    suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String)

    /** Borra (cascada) todos los movimientos de [categoryId]. */
    suspend fun deleteByCategory(categoryId: String)

    /**
     * Confirma la transacción (y su par de transferencia, si aplica). Si la
     * fecha original era futura (venía programada), la reemplaza por [today]
     * — una transacción CONFIRMADA nunca puede quedar con fecha futura.
     */
    suspend fun confirm(id: String, today: LocalDate)
}
