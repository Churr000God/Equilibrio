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
import mx.equilibrio.data.local.Migrations
import mx.equilibrio.data.local.dao.AccountDao
import mx.equilibrio.data.local.dao.GoalDao
import mx.equilibrio.data.local.dao.ReportDao
import mx.equilibrio.data.local.dao.TransactionDao
import mx.equilibrio.data.local.dao.UserDao
import mx.equilibrio.data.repository.AccountRepositoryImpl
import mx.equilibrio.data.repository.GoalRepositoryImpl
import mx.equilibrio.data.repository.ReportRepositoryImpl
import mx.equilibrio.data.repository.TransactionRepositoryImpl
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.GoalRepository
import mx.equilibrio.domain.repository.ReportRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EquilibrioDatabase =
        Room.databaseBuilder(context, EquilibrioDatabase::class.java, EquilibrioDatabase.DATABASE_NAME)
            .addMigrations(*Migrations.ALL)
            .build()

    @Provides
    fun provideUserDao(database: EquilibrioDatabase): UserDao = database.userDao()

    @Provides
    fun provideAccountDao(database: EquilibrioDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransactionDao(database: EquilibrioDatabase): TransactionDao = database.transactionDao()

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
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
}
