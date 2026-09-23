package mx.equilibrio.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mx.equilibrio.data.local.EquilibrioDatabase
import mx.equilibrio.data.local.MIGRATION_1_2
import mx.equilibrio.data.local.MIGRATION_2_3
import mx.equilibrio.data.local.MIGRATION_3_4
import mx.equilibrio.data.local.MIGRATION_4_5
import mx.equilibrio.data.local.MIGRATION_5_6
import mx.equilibrio.data.local.MIGRATION_6_7
import mx.equilibrio.data.local.MIGRATION_7_8
import mx.equilibrio.data.local.MIGRATION_8_9
import mx.equilibrio.data.local.MIGRATION_9_10
import mx.equilibrio.data.local.MIGRATION_10_11
import mx.equilibrio.data.local.MIGRATION_11_12
import mx.equilibrio.data.local.MIGRATION_12_13
import mx.equilibrio.data.local.dao.AccountDao
import mx.equilibrio.data.local.dao.AlertDao
import mx.equilibrio.data.local.dao.CategoryDao
import mx.equilibrio.data.local.dao.GoalDao
import mx.equilibrio.data.local.dao.PeriodDao
import mx.equilibrio.data.local.dao.ReportDao
import mx.equilibrio.data.local.dao.TransactionDao
import mx.equilibrio.data.local.dao.TransferDao
import mx.equilibrio.data.local.dao.UserDao
import mx.equilibrio.data.repository.AccountRepositoryImpl
import mx.equilibrio.data.repository.AlertRepositoryImpl
import mx.equilibrio.data.repository.CategoryRepositoryImpl
import mx.equilibrio.data.repository.GoalRepositoryImpl
import mx.equilibrio.data.repository.PeriodRepositoryImpl
import mx.equilibrio.data.repository.ReportRepositoryImpl
import mx.equilibrio.data.repository.TransactionRepositoryImpl
import mx.equilibrio.data.repository.TransferRepositoryImpl
import mx.equilibrio.data.repository.UserRepositoryImpl
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.AlertRepository
import mx.equilibrio.domain.repository.CategoryRepository
import mx.equilibrio.domain.repository.GoalRepository
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.ReportRepository
import mx.equilibrio.domain.repository.TransactionRepository
import mx.equilibrio.domain.repository.TransferRepository
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EquilibrioDatabase =
        Room.databaseBuilder(context, EquilibrioDatabase::class.java, EquilibrioDatabase.DATABASE_NAME)
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
            )
            .build()

    @Provides
    fun provideUserDao(database: EquilibrioDatabase): UserDao = database.userDao()

    @Provides
    fun provideAccountDao(database: EquilibrioDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransactionDao(database: EquilibrioDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: EquilibrioDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideAlertDao(database: EquilibrioDatabase): AlertDao = database.alertDao()

    @Provides
    fun provideTransferDao(database: EquilibrioDatabase): TransferDao = database.transferDao()

    @Provides
    fun providePeriodDao(database: EquilibrioDatabase): PeriodDao = database.periodDao()

    @Provides
    fun provideGoalDao(database: EquilibrioDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideReportDao(database: EquilibrioDatabase): ReportDao = database.reportDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository

    @Binds
    abstract fun bindPeriodRepository(impl: PeriodRepositoryImpl): PeriodRepository

    @Binds
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
}
