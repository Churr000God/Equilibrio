package mx.equilibrio.domain.model.report

/**
 * El cruce que define el producto: qué tipo de ingreso paga qué tipo de gasto.
 * Cada celda en centavos; `shareOf` la expresa como fracción del ingreso total.
 */
data class CrossMatrix(
    val fixedEssential: Long,
    val fixedRecreational: Long,
    val variableEssential: Long,
    val variableRecreational: Long,
    val incomeCents: Long,
) {
    fun shareOf(cellCents: Long): Float = if (incomeCents == 0L) 0f else cellCents.toFloat() / incomeCents

    val isEmpty: Boolean
        get() = fixedEssential == 0L && fixedRecreational == 0L && variableEssential == 0L && variableRecreational == 0L

    companion object {
        val EMPTY = CrossMatrix(0, 0, 0, 0, 0)
    }
}

/** Lectura en lenguaje simple de la matriz. El texto vive en la UI. */
enum class MatrixInsight {
    /** Sin ingresos ni gastos clasificados en el periodo. */
    NO_DATA,
    /** Hay gasto pero no se registró ningún ingreso. */
    NO_INCOME,
    /** El ingreso fijo no alcanza para lo esencial. */
    ESSENTIAL_UNCOVERED,
    /** Lo esencial sale del fijo y lo recreativo se paga sobre todo con variable. */
    RECREATIONAL_FROM_VARIABLE,
    /** Lo esencial y lo recreativo caben dentro del ingreso fijo. */
    BALANCED,
    /** El gasto total supera el ingreso total. */
    OVERSPENT,
}
