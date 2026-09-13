package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.Account

interface AccountRepository {
    fun observeAll(): Flow<List<Account>>
}
