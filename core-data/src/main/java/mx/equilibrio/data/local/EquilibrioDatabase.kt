package mx.equilibrio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import mx.equilibrio.data.local.dao.AccountDao
import mx.equilibrio.data.local.dao.CategoryDao
import mx.equilibrio.data.local.dao.TransactionDao
import mx.equilibrio.data.local.dao.UserDao
import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.data.local.entity.CategoryEntity
import mx.equilibrio.data.local.entity.TransactionEntity
import mx.equilibrio.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, AccountEntity::class, TransactionEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class EquilibrioDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "equilibrio.db"
    }
}
