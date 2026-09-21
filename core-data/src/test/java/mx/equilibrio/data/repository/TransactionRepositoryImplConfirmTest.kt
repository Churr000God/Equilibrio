package mx.equilibrio.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import mx.equilibrio.data.local.EquilibrioDatabase
import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.data.local.entity.TransferEntity
import mx.equilibrio.data.local.entity.UserEntity
import mx.equilibrio.data.prefs.LocalSession
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Corre bajo Robolectric (sin emulador en este entorno) para poder ejecutarse con
 * `./gradlew test`. Usa una Room in-memory real (no fakes de DAO) porque lo que se
 * prueba es la transacción SQL de confirm(), no solo la firma del método.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class TransactionRepositoryImplConfirmTest {

    private lateinit var db: EquilibrioDatabase
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EquilibrioDatabase::class.java,
        ).allowMainThreadQueries().build()

        val session = LocalSession(
            ApplicationProvider.getApplicationContext(),
            db.userDao(),
            db.accountDao(),
        )
        repository = TransactionRepositoryImpl(db, db.transactionDao(), db.transferDao(), session)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedUserAndAccounts() {
        db.userDao().upsert(
            UserEntity(id = "u1", displayName = "Diego", createdAt = 0, updatedAt = 0, syncState = "PENDING"),
        )
        db.accountDao().upsert(
            AccountEntity(id = "cash", userId = "u1", name = "Efectivo", type = "CASH", updatedAt = 0, syncState = "PENDING"),
        )
        db.accountDao().upsert(
            AccountEntity(id = "bank", userId = "u1", name = "Banco", type = "BANK", updatedAt = 0, syncState = "PENDING"),
        )
    }

    private suspend fun seedTransferPair(): Pair<String, String> {
        seedUserAndAccounts()
        db.transactionDao().upsert(
            TransactionEntity(
                id = "expense",
                userId = "u1",
                accountId = "cash",
                kind = "EXPENSE",
                classification = null,
                amountCents = 500,
                occurredAt = 0,
                note = null,
                updatedAt = 0,
                syncState = "PENDING",
                status = "SCHEDULED",
            ),
        )
        db.transactionDao().upsert(
            TransactionEntity(
                id = "income",
                userId = "u1",
                accountId = "bank",
                kind = "INCOME",
                classification = null,
                amountCents = 500,
                occurredAt = 0,
                note = null,
                updatedAt = 0,
                syncState = "PENDING",
                status = "SCHEDULED",
            ),
        )
        db.transferDao().upsert(
            TransferEntity(id = "tr1", egresoId = "expense", ingresoId = "income", updatedAt = 0, syncState = "PENDING"),
        )
        return "expense" to "income"
    }

    @Test
    fun `confirmar el egreso confirma tambien el ingreso de la misma transferencia`() = runTest {
        val (expenseId, incomeId) = seedTransferPair()

        repository.confirm(expenseId)

        assertEquals("COMPLETED", db.transactionDao().getById(expenseId)?.status)
        assertEquals("COMPLETED", db.transactionDao().getById(incomeId)?.status)
    }

    @Test
    fun `confirmar el ingreso confirma tambien el egreso de la misma transferencia`() = runTest {
        val (expenseId, incomeId) = seedTransferPair()

        repository.confirm(incomeId)

        assertEquals("COMPLETED", db.transactionDao().getById(expenseId)?.status)
        assertEquals("COMPLETED", db.transactionDao().getById(incomeId)?.status)
    }

    @Test
    fun `confirmar una transaccion sin transferencia asociada solo confirma esa fila`() = runTest {
        seedUserAndAccounts()
        db.transactionDao().upsert(
            TransactionEntity(
                id = "solo",
                userId = "u1",
                accountId = "cash",
                kind = "EXPENSE",
                classification = null,
                amountCents = 100,
                occurredAt = 0,
                note = null,
                updatedAt = 0,
                syncState = "PENDING",
                status = "SCHEDULED",
            ),
        )

        repository.confirm("solo")

        assertEquals("COMPLETED", db.transactionDao().getById("solo")?.status)
    }
}
