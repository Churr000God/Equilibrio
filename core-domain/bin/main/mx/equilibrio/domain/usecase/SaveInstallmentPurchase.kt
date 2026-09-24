package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.addMonthsClamped
import mx.equilibrio.domain.model.deriveTransactionStatus
import mx.equilibrio.domain.model.requireWithinCreditLimit
import mx.equilibrio.domain.repository.PeriodRepository
import mx.equilibrio.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Compra a meses en tarjeta: reparte [totalAmountCents] en [installmentCount]
 * cuotas mensuales (resto en la última), cada una con su propio `occurredAt`,
 * periodo y status (SCHEDULED si cae a futuro). Es la misma regla de límite
 * que [SaveCreditPurchase] (caso N=1), validada de una sola vez contra TODAS
 * las cuotas antes de persistir cualquiera — si excede el límite, no se
 * guarda ninguna. No editable: solo [DeleteInstallmentPlan] borra el plan
 * completo.
 */
class SaveInstallmentPurchase @Inject constructor(
    private val getAccount: GetAccount,
    private val getOrCreatePeriodForDate: GetOrCreatePeriodForDate,
    private val periodRepository: PeriodRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        planId: String,
        accountId: String,
        classification: Classification?,
        totalAmountCents: Long,
        installmentCount: Int,
        firstOccurredAt: LocalDate,
        note: String?,
        categoryId: String?,
        userId: String,
        today: LocalDate,
    ): List<Transaction> {
        require(installmentCount >= 2) { "installmentCount debe ser >= 2, fue $installmentCount" }

        val account = requireCreditCardAccount(getAccount, accountId)
        val creditLimitCents = requireNotNull(account.creditLimitCents)

        val baseCents = totalAmountCents / installmentCount
        val remainderCents = totalAmountCents - baseCents * installmentCount
        val targetDay = firstOccurredAt.day

        val installments = (1..installmentCount).map { index ->
            val occurredAt = addMonthsClamped(firstOccurredAt, index - 1, targetDay)
            val amountCents = if (index == installmentCount) baseCents + remainderCents else baseCents
            val period = getOrCreatePeriodForDate(accountId, occurredAt)
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = accountId,
                kind = TransactionKind.EXPENSE,
                classification = classification,
                amountCents = amountCents,
                occurredAt = occurredAt,
                note = note,
                categoryId = categoryId,
                status = deriveTransactionStatus(occurredAt, today),
                periodId = period.id,
                installmentPlanId = planId,
                installmentIndex = index,
                installmentCount = installmentCount,
            )
        }

        val periods = periodRepository.observeByAccount(accountId).first()
        val existingCharges = transactionRepository.observeAll().first()
        requireWithinCreditLimit(creditLimitCents, periods, existingCharges, installments, today)

        installments.forEach { transactionRepository.upsert(it) }
        return installments
    }
}
