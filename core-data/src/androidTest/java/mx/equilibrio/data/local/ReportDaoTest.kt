package mx.equilibrio.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.data.local.entity.CategoryEntity
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.data.local.entity.UserEntity
import mx.equilibrio.data.mapper.toEpochMillis
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportDaoTest {

    private lateinit var database: EquilibrioDatabase

    private val userId = "user-1"
    private val accountId = "account-1"
    private val sepFrom = LocalDate(2026, 9, 1).toEpochMillis()
    private val sepTo = LocalDate(2026, 9, 30).toEpochMillis()

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
        listOf("groceries" to "Súper", "outings" to "Salidas", "music" to "Música", "other" to "Otro").forEachIndexed { i, (id, name) ->
            database.categoryDao().upsert(
                CategoryEntity(id = id, userId = userId, name = name, type = "EXPENSE", colorSlot = 0, icon = "", sortOrder = i, updatedAt = 0, syncState = "PENDING"),
            )
        }
    }

    @After
    fun tearDown() = database.close()

    private suspend fun tx(
        id: String,
        kind: String,
        classification: String?,
        cents: Long,
        date: LocalDate,
        category: String? = null,
        deleted: Boolean = false,
        goalId: String? = null,
        status: String = "COMPLETED",
    ) = database.transactionDao().upsert(
        TransactionEntity(
            id = id, userId = userId, accountId = accountId, kind = kind, classification = classification,
            amountCents = cents, occurredAt = date.toEpochMillis(), note = null, categoryId = category,
            updatedAt = 0, syncState = "PENDING", isDeleted = deleted, status = status, goalId = goalId,
        ),
    )

    @Test
    fun periodTotalsGroupByKindAndClassificationExcludingSavingsAndScheduled() = runBlocking {
        tx("i1", "INCOME", "FIXED", 4200_00, LocalDate(2026, 9, 1))
        tx("i2", "INCOME", "FIXED", 300_00, LocalDate(2026, 9, 15))
        tx("e1", "EXPENSE", "ESSENTIAL", 742_00, LocalDate(2026, 9, 3), category = "groceries")
        tx("e2", "EXPENSE", "RECREATIONAL", 85_00, LocalDate(2026, 9, 4), category = "outings")
        tx("s1", "EXPENSE", null, 220_00, LocalDate(2026, 9, 5), goalId = "g1")
        tx("d1", "EXPENSE", "ESSENTIAL", 999_00, LocalDate(2026, 9, 6), deleted = true)
        tx("o1", "EXPENSE", "ESSENTIAL", 999_00, LocalDate(2026, 8, 31))
        tx("sch", "EXPENSE", "ESSENTIAL", 999_00, LocalDate(2026, 9, 7), status = "SCHEDULED")

        database.reportDao().observePeriodTotals(userId, sepFrom, sepTo).test {
            val rows = awaitItem().associateBy { it.kind to it.classification }
            assertEquals(4500_00L, rows["INCOME" to "FIXED"]!!.totalCents)
            assertEquals(742_00L, rows["EXPENSE" to "ESSENTIAL"]!!.totalCents)
            assertEquals(85_00L, rows["EXPENSE" to "RECREATIONAL"]!!.totalCents)
            assertEquals(3, rows.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun savingsCentsSumsOnlyGoalContributions() = runBlocking {
        tx("s1", "EXPENSE", null, 220_00, LocalDate(2026, 9, 5), goalId = "g1")
        tx("s2", "EXPENSE", null, 80_00, LocalDate(2026, 9, 20), goalId = "g1")
        tx("e1", "EXPENSE", "ESSENTIAL", 742_00, LocalDate(2026, 9, 3))

        database.reportDao().observeSavingsCents(userId, sepFrom, sepTo).test {
            assertEquals(300_00L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthlyTrendGroupsByMonthAndKind() = runBlocking {
        tx("i1", "INCOME", "FIXED", 100_00, LocalDate(2026, 7, 10))
        tx("e1", "EXPENSE", "ESSENTIAL", 40_00, LocalDate(2026, 7, 11))
        tx("i2", "INCOME", "VARIABLE", 200_00, LocalDate(2026, 9, 1))
        tx("s1", "EXPENSE", null, 50_00, LocalDate(2026, 9, 2), goalId = "g1")

        val from = LocalDate(2026, 4, 1).toEpochMillis()
        database.reportDao().observeMonthlyTrend(userId, from, sepTo).test {
            val rows = awaitItem()
            assertEquals(listOf("2026-07", "2026-07", "2026-09"), rows.map { it.month })
            assertEquals(100_00L, rows.first { it.month == "2026-07" && it.kind == "INCOME" }.totalCents)
            assertEquals(40_00L, rows.first { it.month == "2026-07" && it.kind == "EXPENSE" }.totalCents)
            assertEquals(200_00L, rows.first { it.month == "2026-09" }.totalCents)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun categoryTotalsOrderedDescendingExcludingSavingsAndIncome() = runBlocking {
        tx("e1", "EXPENSE", "ESSENTIAL", 742_00, LocalDate(2026, 9, 3), category = "groceries")
        tx("e2", "EXPENSE", "RECREATIONAL", 85_00, LocalDate(2026, 9, 4), category = "outings")
        tx("e3", "EXPENSE", "ESSENTIAL", 60_00, LocalDate(2026, 9, 5), category = "groceries")
        tx("e4", "EXPENSE", "ESSENTIAL", 30_00, LocalDate(2026, 9, 6))
        tx("s1", "EXPENSE", null, 220_00, LocalDate(2026, 9, 5), goalId = "g1")
        tx("i1", "INCOME", "FIXED", 4200_00, LocalDate(2026, 9, 1), category = "other")

        database.reportDao().observeCategoryTotals(userId, sepFrom, sepTo).test {
            val rows = awaitItem()
            assertEquals(listOf("groceries", "outings", null), rows.map { it.categoryId })
            assertEquals("Súper", rows[0].categoryName)
            assertEquals(802_00L, rows[0].totalCents)
            assertEquals(30_00L, rows[2].totalCents)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
