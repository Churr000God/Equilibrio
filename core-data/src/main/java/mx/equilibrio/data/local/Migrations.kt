package mx.equilibrio.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones escritas a mano. `fallbackToDestructiveMigration` está prohibido:
 * borraría datos financieros del usuario.
 */
object Migrations {

    /** v1 → v2: tablas de metas y abonos. Aditiva, no toca datos existentes. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
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
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `goal_contributions` (
                    `id` TEXT NOT NULL,
                    `goal_id` TEXT NOT NULL,
                    `transaction_id` TEXT NOT NULL,
                    `amount_cents` INTEGER NOT NULL,
                    `occurred_at` INTEGER NOT NULL,
                    `updated_at` INTEGER NOT NULL,
                    `sync_state` TEXT NOT NULL,
                    `is_deleted` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`goal_id`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                    FOREIGN KEY(`transaction_id`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_goal_contributions_goal_id` ON `goal_contributions` (`goal_id`)")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_goal_contributions_transaction_id` ON `goal_contributions` (`transaction_id`)",
            )
        }
    }

    val ALL = arrayOf(MIGRATION_1_2)
}
