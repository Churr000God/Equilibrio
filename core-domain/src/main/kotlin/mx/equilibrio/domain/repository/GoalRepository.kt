package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Goal

interface GoalRepository {
    fun observeAll(): Flow<List<Goal>>
    suspend fun getById(id: String): Goal?
    suspend fun upsert(goal: Goal)
    suspend fun delete(id: String)

    /**
     * Registra un abono: crea el movimiento espejo (gasto con categoría
     * `SAVINGS`) y la contribución, y marca la meta como completada si
     * alcanza su objetivo. Todo en una sola transacción de base de datos.
     */
    suspend fun contribute(goalId: String, accountId: String, amountCents: Long, date: LocalDate)

    /** Fechas de todos los abonos vivos del usuario, para calcular la racha. */
    fun observeContributionDates(): Flow<List<LocalDate>>
}
