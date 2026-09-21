package mx.equilibrio.ui.components

/**
 * Reglas del campo de monto compartidas por Registro rápido, abonos y metas:
 * solo dígitos, un punto, dos decimales.
 */
fun sanitizeAmountInput(raw: String): String {
    val filtered = raw.filterIndexed { index, c -> c.isDigit() || (c == '.' && !raw.take(index).contains('.')) }
    val parts = filtered.split(".")
    return if (parts.size > 1) "${parts[0]}.${parts[1].take(2)}" else filtered
}

fun formatAmountInput(cents: Long): String {
    val whole = cents / 100
    val fraction = cents % 100
    return if (fraction == 0L) whole.toString() else "$whole.${fraction.toString().padStart(2, '0')}"
}

fun String.toCentsOrZero(): Long = toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
