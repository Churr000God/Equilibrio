package mx.equilibrio.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EquilibrioDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2KeepsTransactionsAndCreatesGoalTables() {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO users (id, display_name, created_at, updated_at, sync_state, is_deleted) " +
                    "VALUES ('u1', NULL, 0, 0, 'PENDING', 0)",
            )
            execSQL(
                "INSERT INTO accounts (id, user_id, name, type, balance_cents, credit_limit_cents, statement_day, due_day, color_slot, updated_at, sync_state, is_deleted) " +
                    "VALUES ('a1', 'u1', 'Efectivo', 'CASH', 0, NULL, NULL, NULL, 0, 0, 'PENDING', 0)",
            )
            execSQL(
                "INSERT INTO transactions (id, user_id, account_id, kind, classification, amount_cents, occurred_at, note, category_key, updated_at, sync_state, is_deleted) " +
                    "VALUES ('t1', 'u1', 'a1', 'INCOME', 'FIXED', 420000, 0, NULL, NULL, 0, 'PENDING', 0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 2, true, Migrations.MIGRATION_1_2)

        db.query("SELECT amount_cents FROM transactions WHERE id = 't1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(420000L, cursor.getLong(0))
        }
        db.query("SELECT COUNT(*) FROM goals").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0L, cursor.getLong(0))
        }
        db.query("SELECT COUNT(*) FROM goal_contributions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0L, cursor.getLong(0))
        }
    }
}
