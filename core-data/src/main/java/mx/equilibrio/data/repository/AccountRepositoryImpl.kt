package mx.equilibrio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.dao.AccountDao
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.repository.AccountRepository
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao,
    private val session: LocalSession,
) : AccountRepository {

    override fun observeAll(): Flow<List<Account>> = flow {
        emitAll(dao.observeAll(session.currentUserId()).map { list -> list.map { it.toDomain() } })
    }
}
