package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

/**
 * Meta de ahorro. `savedCents` es derivado (suma de abonos), nunca se edita
 * directamente: el repositorio lo calcula al leer.
 */
data class Goal(
    val id: String,
    val userId: String,
    val name: String,
    val targetCents: Long,
    val savedCents: Long,
    val deadline: LocalDate?,
    val status: GoalStatus,
    val createdAt: LocalDate,
) {
    init {
        require(name.isNotBlank()) { "La meta necesita un nombre" }
        require(targetCents > 0) { "targetCents debe ser positivo, fue $targetCents" }
        require(savedCents >= 0) { "savedCents no puede ser negativo, fue $savedCents" }
    }

    val progress: Float get() = (savedCents.toFloat() / targetCents).coerceIn(0f, 1f)

    val remainingCents: Long get() = (targetCents - savedCents).coerceAtLeast(0)

    val isCompleted: Boolean get() = status == GoalStatus.COMPLETED || savedCents >= targetCents
}
