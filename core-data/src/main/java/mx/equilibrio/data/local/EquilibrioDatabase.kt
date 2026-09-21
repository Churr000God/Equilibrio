package mx.equilibrio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import mx.equilibrio.data.local.dao.AccountDao
import mx.equilibrio.data.local.dao.AlertDao
import mx.equilibrio.data.local.dao.CategoryDao
import mx.equilibrio.data.local.dao.GoalDao
import mx.equilibrio.data.local.dao.PeriodDao
import mx.equilibrio.data.local.dao.ReportDao
import mx.equilibrio.data.local.dao.TransactionDao
import mx.equilibrio.data.local.dao.TransferDao
import mx.equilibrio.data.local.dao.UserDao
import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.data.local.entity.AlertEntity
import mx.equilibrio.data.local.entity.CategoryEntity
import mx.equilibrio.data.local.entity.GoalEntity
import mx.equilibrio.data.local.entity.PeriodEntity
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.data.local.entity.TransferEntity
import mx.equilibrio.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        AlertEntity::class,
        TransferEntity::class,
        PeriodEntity::class,
        GoalEntity::class,
    ],
    version = 11,
    exportSchema = true,
)
abstract class EquilibrioDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun alertDao(): AlertDao
    abstract fun transferDao(): TransferDao
    abstract fun periodDao(): PeriodDao
    abstract fun goalDao(): GoalDao
    abstract fun reportDao(): ReportDao

    companion object {
        const val DATABASE_NAME = "equilibrio.db"
    }
}
