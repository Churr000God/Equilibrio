package mx.equilibrio.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE accounts ADD COLUMN last_digits INTEGER DEFAULT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` TEXT NOT NULL,
                `user_id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `color_slot` INTEGER NOT NULL,
                `icon` TEXT NOT NULL,
                `is_system` INTEGER NOT NULL DEFAULT 0,
                `sort_order` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `sync_state` TEXT NOT NULL,
                `is_deleted` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_user_id` ON `categories` (`user_id`)")

        // SQLite can't ALTER a column type/FK in place: rebuild `transactions` with
        // category_key (String) renamed to category_id (String) backed by a real FK.
        db.execSQL(
            """
            CREATE TABLE `transactions_new` (
                `id` TEXT NOT NULL,
                `user_id` TEXT NOT NULL,
                `account_id` TEXT NOT NULL,
                `kind` TEXT NOT NULL,
                `classification` TEXT,
                `amount_cents` INTEGER NOT NULL,
                `occurred_at` INTEGER NOT NULL,
                `note` TEXT,
                `category_id` TEXT,
                `updated_at` INTEGER NOT NULL,
                `sync_state` TEXT NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(`account_id`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `transactions_new`
            (`id`, `user_id`, `account_id`, `kind`, `classification`, `amount_cents`, `occurred_at`, `note`, `category_id`, `updated_at`, `sync_state`, `is_deleted`)
            SELECT `id`, `user_id`, `account_id`, `kind`, `classification`, `amount_cents`, `occurred_at`, `note`, `category_key`, `updated_at`, `sync_state`, `is_deleted`
            FROM `transactions`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `transactions`")
        db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transactions_user_id_occurred_at` ON `transactions` (`user_id`, `occurred_at`)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_account_id` ON `transactions` (`account_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category_id` ON `transactions` (`category_id`)")
    }
}

/** RF01 — perfil de Google en `users` (google_id/email/nombre/foto) + plan freemium (RF10). */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE users ADD COLUMN google_id TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE users ADD COLUMN email TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE users ADD COLUMN given_name TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE users ADD COLUMN family_name TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE users ADD COLUMN photo_url TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE users ADD COLUMN plan TEXT NOT NULL DEFAULT 'FREE'")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_users_google_id` ON `users` (`google_id`)")
    }
}

/** RF09 — tabla `alerts` para los banners de Inicio/Cuentas/Metas. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `alerts` (
                `id` TEXT NOT NULL,
                `user_id` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `reference_id` TEXT,
                `is_read` INTEGER NOT NULL DEFAULT 0,
                `triggered_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_alerts_user_id` ON `alerts` (`user_id`)")
    }
}

/** Transferencias entre cuentas: liga un par egreso/ingreso en `transfers`. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transfers` (
                `id` TEXT NOT NULL,
                `egreso_id` TEXT NOT NULL,
                `ingreso_id` TEXT NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `sync_state` TEXT NOT NULL,
                `is_deleted` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`egreso_id`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(`ingreso_id`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transfers_egreso_id` ON `transfers` (`egreso_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transfers_ingreso_id` ON `transfers` (`ingreso_id`)")
    }
}

/** Movimientos programados a futuro: estado COMPLETED/SCHEDULED en `transactions`. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN status TEXT NOT NULL DEFAULT 'COMPLETED'")
    }
}

/** RF01 — login con email/contraseña: hash Argon2id en `users.password_hash`. */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE users ADD COLUMN password_hash TEXT DEFAULT NULL")
    }
}

/**
 * RF01 — el registro por email debe fallar si el correo ya está en uso; sin UNIQUE en
 * `users.email` un registro concurrente podía crear dos filas con el mismo correo.
 * Antes de indexar: deja solo la fila más antigua por correo repetido (limpia posibles
 * duplicados de antes de este fix) para que CREATE UNIQUE INDEX no falle. Un índice
 * UNIQUE en SQLite no considera duplicados a los NULL (usuarios solo-Google sin email),
 * así que no hace falta backfill para esas filas.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            UPDATE users SET email = NULL
            WHERE email IS NOT NULL
              AND created_at > (SELECT MIN(u2.created_at) FROM users u2 WHERE u2.email = users.email)
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
    }
}

/** Periodos de facturación de tarjeta de crédito: tabla `periods` + `transactions.period_id`. */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `periods` (
                `id` TEXT NOT NULL,
                `account_id` TEXT NOT NULL,
                `start_at` INTEGER NOT NULL,
                `end_at` INTEGER NOT NULL,
                `pay_at` INTEGER NOT NULL,
                `state` TEXT NOT NULL DEFAULT 'OPEN',
                `carried_balance_cents` INTEGER NOT NULL DEFAULT 0,
                `amount_paid_cents` INTEGER NOT NULL DEFAULT 0,
                `updated_at` INTEGER NOT NULL,
                `sync_state` TEXT NOT NULL,
                `is_deleted` INTEGER NOT NULL DEFAULT 0,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`account_id`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_periods_account_id` ON `periods` (`account_id`)")

        db.execSQL(
            "ALTER TABLE transactions ADD COLUMN period_id TEXT " +
                "REFERENCES `periods`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION DEFAULT NULL",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_period_id` ON `transactions` (`period_id`)")
    }
}

/**
 * RF08 — tabla `goals` y `transactions.goal_id` (abono espejo de una meta). Portada del PR
 * `feature/metas-reportes` (Persona 2): esa rama se ramificó antes de Login/tarjetas de
 * crédito y traía esto como `MIGRATION_7_8`, ya ocupado en main por `password_hash` —
 * renumerada a 10→11 al integrar.
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `goals` (
                `id` TEXT NOT NULL,
                `user_id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `target_cents` INTEGER NOT NULL,
                `deadline` INTEGER,
                `status` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `sync_state` TEXT NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_user_id` ON `goals` (`user_id`)")
        // Sin REFERENCES a propósito: una meta borrada no debe arrastrar ni bloquear
        // el borrado de sus abonos espejo (el dinero ya salió de la cuenta).
        db.execSQL("ALTER TABLE transactions ADD COLUMN goal_id TEXT DEFAULT NULL")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_goal_id` ON `transactions` (`goal_id`)")
    }
}

/** Compras a meses (installments): agrupa cuotas de una compra en `transactions`. */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN installment_plan_id TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE transactions ADD COLUMN installment_index INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE transactions ADD COLUMN installment_count INTEGER DEFAULT NULL")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transactions_installment_plan_id` " +
                "ON `transactions` (`installment_plan_id`)",
        )
    }
}

/** Detalle de categoría: presupuesto mensual opcional en `categories`. */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN monthly_budget_cents INTEGER DEFAULT NULL")
    }
}

/**
 * Transacciones recurrentes: plantilla propia en `recurring_transactions` +
 * `transactions.recurring_series_id`. Sin FK a la serie (mismo criterio que
 * `goal_id`, ver comentario en MIGRATION_10_11): borrar la plantilla no debe
 * arrastrar ni bloquear el borrado de las ocurrencias ya generadas.
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recurring_transactions` (
                `id` TEXT NOT NULL,
                `user_id` TEXT NOT NULL,
                `account_id` TEXT NOT NULL,
                `kind` TEXT NOT NULL,
                `classification` TEXT NOT NULL,
                `amount_cents` INTEGER NOT NULL,
                `note` TEXT,
                `category_id` TEXT,
                `frequency` TEXT NOT NULL,
                `anchor_day` INTEGER,
                `next_occurrence_at` INTEGER NOT NULL,
                `is_active` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `sync_state` TEXT NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_user_id` ON `recurring_transactions` (`user_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_account_id` ON `recurring_transactions` (`account_id`)")

        db.execSQL("ALTER TABLE transactions ADD COLUMN recurring_series_id TEXT DEFAULT NULL")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transactions_recurring_series_id` " +
                "ON `transactions` (`recurring_series_id`)",
        )
    }
}
