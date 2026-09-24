package mx.equilibrio.domain.usecase

import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Garantiza que exista una categoría "Otros" de [type] para el usuario,
 * creándola si hace falta. Es el fallback al que caen los movimientos sin
 * categoría propia (p. ej. tras borrar una categoría sin reasignar, ver
 * [DeleteCategory]) para que nunca quede una transacción sin categoría
 * válida.
 */
class EnsureOtrosCategory @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(userId: String, type: CategoryType): Category =
        repository.getOrCreateOtros(userId, type)
}
