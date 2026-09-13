package mx.equilibrio.domain.model

/** El cruce que define el producto: tipo de ingreso o tipo de gasto. */
enum class Classification {
    FIXED,
    VARIABLE,
    ESSENTIAL,
    RECREATIONAL,
}

fun Classification.isValidFor(kind: TransactionKind): Boolean = when (kind) {
    TransactionKind.INCOME -> this == Classification.FIXED || this == Classification.VARIABLE
    TransactionKind.EXPENSE -> this == Classification.ESSENTIAL || this == Classification.RECREATIONAL
}
