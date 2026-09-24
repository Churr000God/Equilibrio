package mx.equilibrio.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import mx.equilibrio.data.local.EquilibrioDatabase
import mx.equilibrio.data.local.dao.GoalDao
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.mapper.toEntity
import mx.equilibrio.data.mapper.toEpochMillis
import mx.equilibrio.data.mapper.toLocalDate
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.domain.model.Goal
import mx.equilibrio.domain.model.GoalStatus
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.repository.GoalRepository
import java.util.UUID
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val database: EquilibrioDatabase,
    private val dao: GoalDao,
    private val session: LocalSession,
) : GoalRepository {

    override fun observeAll(): Flow<List<Goal>> = flow {
        emitAll(dao.observeAll(session.currentUserId()).map { list -> list.map { it.toDomain() } })
    }

    override suspend fun getById(id: String): Goal? = dao.getById(id)?.toDomain()

    override suspend fun upsert(goal: Goal) {
        dao.upsert(goal.toEntity(syncState = "PENDING", updatedAt = System.currentTimeMillis()))
    }

    /** Los abonos ya hechos se conservan como movimientos: el dinero sí salió de la cuenta. */
    override suspend fun delete(id: String) {
        dao.markDeleted(id, now = System.currentTimeMillis())
    }

    // Un abono a una meta se registra como un gasto real (mueve dinero de la cuenta) y, dentro
    // de la misma transacción de BD, revisa si con este abono se alcanzó el objetivo para
    // marcar la meta como completada. Ambas escrituras van atómicas para que la meta nunca
    // quede en estado inconsistente con sus propios abonos.
    override suspend fun contribute(goalId: String, accountId: String, amountCents: Long, date: LocalDate) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            val goal = dao.getById(goalId) ?: error("Meta $goalId no existe")

            database.transactionDao().upsert(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = goal.goal.userId,
                    accountId = accountId,
                    kind = TransactionKind.EXPENSE.name,
                    classification = null,
                    amountCents = amountCents,
                    occurredAt = date.toEpochMillis(),
                    note = "Abono a ${goal.goal.name}",
                    categoryId = null,
                    updatedAt = now,
                    syncState = "PENDING",
                    status = TransactionStatus.COMPLETED.name,
                    goalId = goalId,
                ),
            )

            // Solo promueve ACTIVE -> COMPLETED; si la meta ya estaba en otro estado
            // (p. ej. ya completada) este abono extra no debe reescribirlo.
            val reached = goal.savedCents + amountCents >= goal.goal.targetCents
            if (reached && goal.goal.status == GoalStatus.ACTIVE.name) {
                dao.updateStatus(goalId, GoalStatus.COMPLETED.name, now)
            }
        }
    }

    override fun observeContributionDates(): Flow<List<LocalDate>> = flow {
        emitAll(dao.observeContributionDates(session.currentUserId()).map { list -> list.map { it.toLocalDate() } })
    }
}
