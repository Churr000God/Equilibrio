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
import mx.equilibrio.data.local.dao.AccountDao
import mx.equilibrio.data.local.dao.TransactionDao
import mx.equilibrio.data.local.dao.UserDao
import mx.equilibrio.data.repository.AccountRepositoryImpl
import mx.equilibrio.data.repository.TransactionRepositoryImpl
import mx.equilibrio.domain.repository.AccountRepository
import mx.equilibrio.domain.repository.TransactionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EquilibrioDatabase =
        Room.databaseBuilder(context, EquilibrioDatabase::class.java, EquilibrioDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideUserDao(database: EquilibrioDatabase): UserDao = database.userDao()

    @Provides
    fun provideAccountDao(database: EquilibrioDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransactionDao(database: EquilibrioDatabase): TransactionDao = database.transactionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
}
