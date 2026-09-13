package mx.equilibrio.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.equilibrio.data.local.dao.AccountDao
import mx.equilibrio.data.local.dao.UserDao
import mx.equilibrio.data.local.entity.AccountEntity
import mx.equilibrio.data.local.entity.UserEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "equilibrio_session")
private val KEY_USER_ID = stringPreferencesKey("user_id")

/**
 * Sin login todavía: genera un userId local una sola vez y siembra la cuenta
 * "Efectivo" por defecto, para que transactions.account_id nunca sea nulo.
 */
@Singleton
class LocalSession @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val accountDao: AccountDao,
) {
    private val mutex = Mutex()
    private var cachedUserId: String? = null

    suspend fun currentUserId(): String {
        cachedUserId?.let { return it }

        return mutex.withLock {
            cachedUserId?.let { return@withLock it }

            val existing = context.dataStore.data.first()[KEY_USER_ID]
            val userId = existing ?: createLocalUser()
            cachedUserId = userId
            userId
        }
    }

    private suspend fun createLocalUser(): String {
        val userId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        userDao.upsert(
            UserEntity(
                id = userId,
                displayName = null,
                createdAt = now,
                updatedAt = now,
                syncState = "PENDING",
            ),
        )

        accountDao.upsert(
            AccountEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Efectivo",
                type = "CASH",
                updatedAt = now,
                syncState = "PENDING",
            ),
        )

        context.dataStore.edit { it[KEY_USER_ID] = userId }
        return userId
    }
}
