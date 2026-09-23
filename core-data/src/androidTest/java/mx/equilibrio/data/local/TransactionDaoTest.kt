package mx.equilibrio.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.data.local.entity.UserEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

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
    fun tearDown() {
        database.close()
    }

    private fun transaction(
        id: String,
        kind: String,
        amountCents: Long,
        occurredAt: Long = 0,
        isDeleted: Boolean = false,
    ) = TransactionEntity(
        id = id,
        userId = userId,
        accountId = accountId,
        kind = kind,
        classification = null,
        amountCents = amountCents,
        occurredAt = occurredAt,
        note = null,
        updatedAt = 0,
        syncState = "PENDING",
        isDeleted = isDeleted,
    )

    @Test
    fun upsertThenGetById() = runBlocking {
        val entity = transaction("t1", "INCOME", 5000_00)
        database.transactionDao().upsert(entity)

        val fetched = database.transactionDao().getById("t1")
        assertEquals(entity, fetched)
    }

    @Test
    fun observeAllExcludesDeleted() = runBlocking {
        database.transactionDao().upsert(transaction("t1", "INCOME", 100, isDeleted = false))
        database.transactionDao().upsert(transaction("t2", "EXPENSE", 50, isDeleted = true))

        database.transactionDao().observeAll(userId).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("t1", list.first().id)
        }
    }

    @Test
    fun markDeletesRemovesFromObserveAll() = runBlocking {
        database.transactionDao().upsert(transaction("t1", "INCOME", 5000_00))
        database.transactionDao().upsert(transaction("t2", "EXPENSE", 500_00))

        database.transactionDao().markDeleted("t2", now = 123L)

        database.transactionDao().observeAll(userId).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertTrue(list.none { it.id == "t2" })
        }
        assertNull(database.transactionDao().getById("t2"))
    }
}
