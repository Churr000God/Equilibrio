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
