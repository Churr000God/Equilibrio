package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType

/**
 * Cuenta CREDIT_CARD válida para registrar cargos: existe, es tarjeta, y
 * tiene límite configurado. Compartido por [SaveCreditPurchase] y
 * [SaveInstallmentPurchase] para no duplicar el trío de validaciones.
 */
internal suspend fun requireCreditCardAccount(getAccount: GetAccount, accountId: String): Account {
    val account = getAccount(accountId)
    requireNotNull(account) { "No existe la cuenta $accountId" }
    require(account.type == AccountType.CREDIT_CARD) {
        "Esta operación solo aplica a cuentas CREDIT_CARD, no ${account.type}"
    }
    requireNotNull(account.creditLimitCents) { "La cuenta $accountId no tiene creditLimitCents configurado" }
    return account
}
