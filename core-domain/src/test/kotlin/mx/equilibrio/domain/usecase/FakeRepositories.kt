package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.Transfer
import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.RecurringTransactionRepository
import mx.equilibrio.domain.repository.TransactionRepository
import mx.equilibrio.domain.repository.TransferRepository
import mx.equilibrio.domain.repository.UserRepository

/** Fake mínimo para probar los use cases de login sin depender de UserRepositoryImpl. */
class FakeUserRepository(
    private var signInResult: Result<User> = Result.failure(IllegalStateException("no configurado")),
    private var registerResult: Result<User> = Result.failure(IllegalStateException("no configurado")),
) : UserRepository {

    var lastSignInArgs: Pair<String, String>? = null
        private set
    var lastRegisterArgs: Triple<String?, String, String>? = null
        private set

    override suspend fun getCurrentUser(): User? = null
    override fun observeCurrentUser(): Flow<User?> = MutableStateFlow(null)

    override suspend fun signInWithGoogle(
        googleId: String,
        email: String?,
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User = throw NotImplementedError("no usado en estos tests")

    override suspend fun signOut() = Unit

    override suspend fun updateProfile(
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User = throw NotImplementedError("no usado en estos tests")

    override suspend fun registerWithPassword(displayName: String?, email: String, password: String): Result<User> {
        lastRegisterArgs = Triple(displayName, email, password)
        return registerResult
    }

    override suspend fun signInWithPassword(email: String, password: String): Result<User> {
        lastSignInArgs = email to password
        return signInResult
    }

    override fun observeAccessibilityMode(): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setAccessibilityMode(enabled: Boolean) = Unit
}

/** Fake de cuentas: devuelve lo que se le precarga por id, sin persistencia real. */
class FakeAccountRepository(
    private val accountsById: Map<String, Account> = emptyMap(),
) : AccountRepository {
    override fun observeAll(): Flow<List<Account>> = MutableStateFlow(accountsById.values.toList())
    override suspend fun count(): Int = accountsById.size
    override suspend fun getById(id: String): Account? = accountsById[id]
    override suspend fun upsert(account: Account) = throw NotImplementedError("no usado en estos tests")
    override suspend fun delete(id: String) = throw NotImplementedError("no usado en estos tests")
}

/** Fake de transferencias: registra el último par (egreso, ingreso) creado. */
class FakeTransferRepository(
    private val idGenerator: () -> String = { "transfer-fake" },
) : TransferRepository {
    var lastCreateArgs: Pair<Transaction, Transaction>? = null
        private set

    override suspend fun create(expense: Transaction, income: Transaction): Transfer {
        lastCreateArgs = expense to income
        return Transfer(id = idGenerator(), expenseTransactionId = expense.id, incomeTransactionId = income.id)
    }

    override suspend fun findByTransactionId(transactionId: String): Transfer? = null
}

/**
 * Fake de transacciones respaldado por un mapa mutable: `observeAll` se recalcula
 * en cada colecta (via `flow { }`) para que un `upsert` seguido de `.first()` en
 * el mismo test vea el estado actualizado, sin necesidad de un StateFlow manual.
 */
class FakeTransactionRepository : TransactionRepository {
    val confirmedIds = mutableListOf<String>()
    private val transactionsById = mutableMapOf<String, Transaction>()

    fun seed(vararg transactions: Transaction) {
        transactions.forEach { transactionsById[it.id] = it }
    }

    override fun observeAll(): Flow<List<Transaction>> = flow { emit(transactionsById.values.toList()) }
    override suspend fun getById(id: String): Transaction? = transactionsById[id]
    override suspend fun upsert(transaction: Transaction) {
        transactionsById[transaction.id] = transaction
    }
    override suspend fun delete(id: String) = throw NotImplementedError("no usado en estos tests")

    override suspend fun markInstallmentPlanDeleted(planId: String) {
        transactionsById.values
            .filter { it.installmentPlanId == planId }
            .forEach { transactionsById.remove(it.id) }
    }

    override fun observeByCategory(categoryId: String): Flow<List<Transaction>> = flow {
        emit(transactionsById.values.filter { it.categoryId == categoryId })
    }

    override fun observeByRecurringSeries(seriesId: String): Flow<List<Transaction>> = flow {
        emit(transactionsById.values.filter { it.recurringSeriesId == seriesId })
    }

    override suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String) {
        transactionsById.values
            .filter { it.categoryId == fromCategoryId }
            .forEach { transactionsById[it.id] = it.copy(categoryId = toCategoryId) }
    }

    override suspend fun deleteByCategory(categoryId: String) {
        transactionsById.values
            .filter { it.categoryId == categoryId }
            .forEach { transactionsById.remove(it.id) }
    }

    override suspend fun confirm(id: String, today: LocalDate) {
        confirmedIds += id
        transactionsById[id]?.let { transaction ->
            val occurredAt = if (transaction.occurredAt > today) today else transaction.occurredAt
            transactionsById[id] = transaction.copy(status = TransactionStatus.COMPLETED, occurredAt = occurredAt)
        }
    }
}

/**
 * Fake de periodos respaldado por un mapa mutable, mismo patrón que
 * [FakeTransactionRepository]: `observeByAccount` se recalcula en cada colecta.
 */
class FakePeriodRepository : PeriodRepository {
    private val periodsById = mutableMapOf<String, Period>()

    fun seed(vararg periods: Period) {
        periods.forEach { periodsById[it.id] = it }
    }

    fun all(): List<Period> = periodsById.values.toList()

    override fun observeByAccount(accountId: String): Flow<List<Period>> = flow {
        emit(periodsById.values.filter { it.accountId == accountId }.sortedBy { it.startAt })
    }

    override suspend fun getActiveByAccount(accountId: String): List<Period> =
        periodsById.values
            .filter { it.accountId == accountId && it.state != PeriodState.CLOSED }
            .sortedBy { it.startAt }

    override suspend fun getById(id: String): Period? = periodsById[id]

    override suspend fun upsert(period: Period) {
        periodsById[period.id] = period
    }
}

/**
 * Fake de series recurrentes, mismo molde que [FakePeriodRepository]: `observeAll` se
 * recalcula en cada colecta (`flow { }`, no `StateFlow`) para que el catch-up de
 * [GenerateDueRecurringTransactions] vea estado fresco entre iteraciones del `while(true)`.
 */
class FakeRecurringTransactionRepository : RecurringTransactionRepository {
    private val seriesById = mutableMapOf<String, RecurringTransaction>()

    fun seed(vararg series: RecurringTransaction) {
        series.forEach { seriesById[it.id] = it }
    }

    fun all(): List<RecurringTransaction> = seriesById.values.toList()

    override fun observeAll(): Flow<List<RecurringTransaction>> = flow { emit(seriesById.values.toList()) }
    override suspend fun getById(id: String): RecurringTransaction? = seriesById[id]
    override suspend fun upsert(series: RecurringTransaction) {
        seriesById[series.id] = series
    }

    override suspend fun setActive(id: String, isActive: Boolean) {
        seriesById[id]?.let { seriesById[id] = it.copy(isActive = isActive) }
    }

    override suspend fun delete(id: String) {
        seriesById.remove(id)
    }
}
