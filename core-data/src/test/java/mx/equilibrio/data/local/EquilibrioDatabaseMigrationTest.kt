package mx.equilibrio.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Corre bajo Robolectric (no hay emulador/adb en este entorno) para poder ejecutarse
 * con `./gradlew test`. MigrationTestHelper valida el schema resultante de cada paso
 * contra el .json exportado en core-data/schemas.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class EquilibrioDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EquilibrioDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    private fun insertUser(db: androidx.sqlite.db.SupportSQLiteDatabase, id: String, email: String?, createdAt: Long) {
        db.execSQL(
            """
            INSERT INTO users
            (id, display_name, created_at, updated_at, sync_state, is_deleted, google_id, email, given_name, family_name, photo_url, plan)
            VALUES ('$id', 'Diego', $createdAt, $createdAt, 'PENDING', 0, NULL, ${email?.let { "'$it'" } ?: "NULL"}, NULL, NULL, NULL, 'FREE')
            """.trimIndent(),
        )
    }

    @Test
    fun `6 a 7 agrega status a transactions con default COMPLETED y preserva filas`() {
        var db = helper.createDatabase(TEST_DB, 6)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.execSQL(
            """
            INSERT INTO accounts (id, user_id, name, type, balance_cents, credit_limit_cents, statement_day, due_day, color_slot, last_digits, updated_at, sync_state, is_deleted)
            VALUES ('a1', 'u1', 'Efectivo', 'CASH', 0, NULL, NULL, NULL, 0, NULL, 0, 'PENDING', 0)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO transactions (id, user_id, account_id, kind, classification, amount_cents, occurred_at, note, category_id, updated_at, sync_state, is_deleted)
            VALUES ('t1', 'u1', 'a1', 'EXPENSE', NULL, 500, 0, NULL, NULL, 0, 'PENDING', 0)
            """.trimIndent(),
        )
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        val cursor = db.query("SELECT status FROM transactions WHERE id = 't1'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals("COMPLETED", cursor.getString(0))
        cursor.close()
        db.close()
    }

    @Test
    fun `7 a 8 agrega password_hash nullable sin perder usuarios existentes`() {
        var db = helper.createDatabase(TEST_DB, 7)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, MIGRATION_7_8)

        val cursor = db.query("SELECT password_hash FROM users WHERE id = 'u1'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertNull(cursor.getString(0))
        cursor.close()
        db.close()
    }

    @Test
    fun `8 a 9 deduplica correos repetidos antes de crear el indice UNIQUE`() {
        var db = helper.createDatabase(TEST_DB, 8)
        // Dos usuarios con el mismo correo (posible antes del fix): debe sobrevivir
        // solo el más antiguo (menor created_at), el resto queda con email = NULL.
        insertUser(db, "old", "dup@equilibrio.mx", 100)
        insertUser(db, "new", "dup@equilibrio.mx", 200)
        insertUser(db, "unico", "unico@equilibrio.mx", 150)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        val emailCursor = db.query("SELECT id, email FROM users ORDER BY id")
        val emailsById = mutableMapOf<String, String?>()
        while (emailCursor.moveToNext()) {
            emailsById[emailCursor.getString(0)] = if (emailCursor.isNull(1)) null else emailCursor.getString(1)
        }
        emailCursor.close()

        assertEquals("dup@equilibrio.mx", emailsById["old"])
        assertNull(emailsById["new"])
        assertEquals("unico@equilibrio.mx", emailsById["unico"])

        // El índice UNIQUE debe existir y rechazar un duplicado nuevo.
        db.execSQL(
            "INSERT INTO users (id, display_name, created_at, updated_at, sync_state, is_deleted, plan) " +
                "VALUES ('otro', 'X', 300, 300, 'PENDING', 0, 'FREE')",
        )
        var threw = false
        try {
            db.execSQL(
                "UPDATE users SET email = 'unico@equilibrio.mx' WHERE id = 'otro'",
            )
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            threw = true
        }
        assertEquals(true, threw)
        db.close()
    }

    @Test
    fun `9 a 10 crea periods y agrega period_id nullable a transactions sin perder filas`() {
        var db = helper.createDatabase(TEST_DB, 9)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.execSQL(
            """
            INSERT INTO accounts (id, user_id, name, type, balance_cents, credit_limit_cents, statement_day, due_day, color_slot, last_digits, updated_at, sync_state, is_deleted)
            VALUES ('cc1', 'u1', 'Tarjeta', 'CREDIT_CARD', 0, 500000, 20, 5, 0, NULL, 0, 'PENDING', 0)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO transactions (id, user_id, account_id, kind, classification, amount_cents, occurred_at, note, category_id, updated_at, sync_state, is_deleted, status)
            VALUES ('t1', 'u1', 'cc1', 'EXPENSE', NULL, 500, 0, NULL, NULL, 0, 'PENDING', 0, 'COMPLETED')
            """.trimIndent(),
        )
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        val txCursor = db.query("SELECT period_id FROM transactions WHERE id = 't1'")
        assertEquals(1, txCursor.count)
        txCursor.moveToFirst()
        assertNull(txCursor.getString(0))
        txCursor.close()

        db.execSQL(
            """
            INSERT INTO periods (id, account_id, start_at, end_at, pay_at, state, carried_balance_cents, amount_paid_cents, updated_at, sync_state, is_deleted, created_at)
            VALUES ('p1', 'cc1', 0, 100, 105, 'OPEN', 0, 0, 0, 'PENDING', 0, 0)
            """.trimIndent(),
        )
        db.execSQL("UPDATE transactions SET period_id = 'p1' WHERE id = 't1'")
        val linkedCursor = db.query("SELECT period_id FROM transactions WHERE id = 't1'")
        linkedCursor.moveToFirst()
        assertEquals("p1", linkedCursor.getString(0))
        linkedCursor.close()
        db.close()
    }

    @Test
    fun `cadena completa 6 a 10 corre sin romper el schema exportado`() {
        var db = helper.createDatabase(TEST_DB, 6)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.close()

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            10,
            true,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
        )
        db.close()
    }

    @Test
    fun `11 a 12 agrega columnas de cuotas nullable y preserva transacciones existentes`() {
        var db = helper.createDatabase(TEST_DB, 9)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.close()
        db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)
        db.close()
        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        db.execSQL(
            """
            INSERT INTO accounts (id, user_id, name, type, balance_cents, credit_limit_cents, statement_day, due_day, color_slot, last_digits, updated_at, sync_state, is_deleted)
            VALUES ('cc1', 'u1', 'Tarjeta', 'CREDIT_CARD', 0, 300000, 20, 5, 0, NULL, 0, 'PENDING', 0)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO transactions (id, user_id, account_id, kind, classification, amount_cents, occurred_at, note, category_id, updated_at, sync_state, is_deleted, status)
            VALUES ('t1', 'u1', 'cc1', 'EXPENSE', NULL, 500, 0, NULL, NULL, 0, 'PENDING', 0, 'COMPLETED')
            """.trimIndent(),
        )
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 12, true, MIGRATION_11_12)

        val cursor = db.query(
            "SELECT amount_cents, installment_plan_id, installment_index, installment_count " +
                "FROM transactions WHERE id = 't1'",
        )
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals(500, cursor.getLong(0))
        assertNull(cursor.getString(1))
        assertTrue(cursor.isNull(2))
        assertTrue(cursor.isNull(3))
        cursor.close()

        db.execSQL(
            """
            INSERT INTO transactions (id, user_id, account_id, kind, classification, amount_cents, occurred_at, note, category_id, updated_at, sync_state, is_deleted, status, installment_plan_id, installment_index, installment_count)
            VALUES ('t2', 'u1', 'cc1', 'EXPENSE', NULL, 334, 0, NULL, NULL, 0, 'PENDING', 0, 'SCHEDULED', 'plan1', 3, 3)
            """.trimIndent(),
        )
        val planCursor = db.query(
            "SELECT installment_plan_id, installment_index, installment_count FROM transactions WHERE id = 't2'",
        )
        planCursor.moveToFirst()
        assertEquals("plan1", planCursor.getString(0))
        assertEquals(3, planCursor.getInt(1))
        assertEquals(3, planCursor.getInt(2))
        planCursor.close()
        db.close()
    }

    @Test
    fun `cadena completa 6 a 12 corre sin romper el schema exportado`() {
        var db = helper.createDatabase(TEST_DB, 6)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.close()

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            12,
            true,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
        )
        db.close()
    }

    @Test
    fun `12 a 13 agrega monthly_budget_cents nullable a categories y preserva filas`() {
        var db = helper.createDatabase(TEST_DB, 9)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.close()
        db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)
        db.close()
        db = helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        db.close()
        db = helper.runMigrationsAndValidate(TEST_DB, 12, true, MIGRATION_11_12)
        db.execSQL(
            """
            INSERT INTO categories (id, user_id, name, type, color_slot, icon, is_system, sort_order, updated_at, sync_state, is_deleted)
            VALUES ('c1', 'u1', 'Comida', 'EXPENSE', 0, 'food', 0, 0, 0, 'PENDING', 0)
            """.trimIndent(),
        )
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 13, true, MIGRATION_12_13)

        val cursor = db.query("SELECT name, monthly_budget_cents FROM categories WHERE id = 'c1'")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals("Comida", cursor.getString(0))
        assertTrue(cursor.isNull(1))
        cursor.close()

        db.execSQL("UPDATE categories SET monthly_budget_cents = 500000 WHERE id = 'c1'")
        val budgetCursor = db.query("SELECT monthly_budget_cents FROM categories WHERE id = 'c1'")
        budgetCursor.moveToFirst()
        assertEquals(500000, budgetCursor.getLong(0))
        budgetCursor.close()
        db.close()
    }

    @Test
    fun `cadena completa 6 a 13 corre sin romper el schema exportado`() {
        var db = helper.createDatabase(TEST_DB, 6)
        insertUser(db, "u1", "u1@equilibrio.mx", 0)
        db.close()

        db = helper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
        )
        db.close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
