package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.repository.AccountRepository
import javax.inject.Inject

/** Cuenta solo las cuentas que consumen cupo freemium (todas excepto CASH). */
class GetAccountCountUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(): Int = repository.countNonCash()
}
