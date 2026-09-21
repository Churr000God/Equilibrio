package mx.equilibrio.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.data.local.entity.GoalEntity
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.data.local.entity.UserEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalDaoTest {

    private lateinit var database: EquilibrioDatabase

    private val userId = "user-1"
    private val accountId = "account-1"

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, EquilibrioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        database.userDao().upsert(
            UserEntity(id = userId, displayName = null, createdAt = 0, updatedAt = 0, syncState = "PENDING"),
        )
        database.accountDao().upsert(
            AccountEntity(id = accountId, userId = userId, name = "Efectivo", type = "CASH", updatedAt = 0, syncState = "PENDING"),
        )
    }

    @After
    fun tearDown() = database.close()

    private fun goal(id: String, target: Long = 6000_00, createdAt: Long = 0) = GoalEntity(
        id = id, userId = userId, name = "Meta $id", targetCents = target, deadline = null,
        status = "ACTIVE", createdAt = createdAt, updatedAt = 0, syncState = "PENDING",
    )

    private suspend fun contribute(goalId: String, id: String, cents: Long, at: Long = 0, deleted: Boolean = false, status: String = "COMPLETED") {
        database.transactionDao().upsert(
            TransactionEntity(
                id = id, userId = userId, accountId = accountId, kind = "EXPENSE", classification = null,
                amountCents = cents, occurredAt = at, note = null, updatedAt = 0, syncState = "PENDING",
                isDeleted = deleted, status = status, goalId = goalId,
            ),
        )
    }

    @Test
    fun savedCentsIsSumOfLiveCompletedContributions() = runBlocking {
        database.goalDao().upsert(goal("g1"))
        contribute("g1", "c1", 200_00)
        contribute("g1", "c2", 20_00)
        contribute("g1", "c3", 999_00, deleted = true)
        contribute("g1", "c4", 555_00, status = "SCHEDULED")

        assertEquals(220_00L, database.goalDao().getById("g1")!!.savedCents)
    }

    @Test
    fun goalWithoutContributionsHasZeroSaved() = runBlocking {
        database.goalDao().upsert(goal("g1"))
        assertEquals(0L, database.goalDao().getById("g1")!!.savedCents)
    }

    @Test
    fun observeAllOrdersNewestFirstAndExcludesDeleted() = runBlocking {
        database.goalDao().upsert(goal("old", createdAt = 1))
        database.goalDao().upsert(goal("new", createdAt = 2))
        database.goalDao().upsert(goal("gone", createdAt = 3))
        database.goalDao().markDeleted("gone", now = 9)

        database.goalDao().observeAll(userId).test {
            assertEquals(listOf("new", "old"), awaitItem().map { it.goal.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletingMirrorTransactionRemovesContribution() = runBlocking {
        database.goalDao().upsert(goal("g1"))
        contribute("g1", "c1", 100_00)
        database.transactionDao().markDeleted("c1", now = 1)

        assertEquals(0L, database.goalDao().getById("g1")!!.savedCents)
    }

    @Test
    fun deletingGoalHidesItsContributionDates() = runBlocking {
        database.goalDao().upsert(goal("g1"))
        contribute("g1", "c1", 100_00, at = 1000)
        database.goalDao().markDeleted("g1", now = 1)

        assertNull(database.goalDao().getById("g1"))
        database.goalDao().observeContributionDates(userId).test {
            assertEquals(emptyList<Long>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateStatusPersists() = runBlocking {
        database.goalDao().upsert(goal("g1"))
        database.goalDao().updateStatus("g1", "COMPLETED", now = 5)
        assertEquals("COMPLETED", database.goalDao().getById("g1")!!.goal.status)
    }
}
