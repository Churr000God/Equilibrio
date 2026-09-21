package mx.equilibrio.feature.reports

import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.model.report.MatrixInsight

private val MONTHS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

fun YearMonth.label(): String = "${MONTHS[month - 1].replaceFirstChar { it.uppercase() }} $year"

fun YearMonth.shortLabel(): String = MONTHS[month - 1].take(3).replaceFirstChar { it.uppercase() }

/** Tono §12: sin exclamaciones, sin culpa, con una lectura concreta. */
fun MatrixInsight.text(): String = when (this) {
    MatrixInsight.NO_DATA -> "Cuando registres ingresos y gastos clasificados, aquí verás de dónde sale cada uno."
    MatrixInsight.NO_INCOME -> "Este mes solo hay gastos. Registra de dónde viene tu dinero para leer el cruce."
    MatrixInsight.OVERSPENT -> "Este mes gastaste más de lo que entró. Revisa qué gasto recreativo puede esperar."
    MatrixInsight.ESSENTIAL_UNCOVERED -> "Tu ingreso fijo no alcanza para lo esencial: parte se paga con ingreso variable, que no siempre llega igual."
    MatrixInsight.RECREATIONAL_FROM_VARIABLE -> "Casi todo lo recreativo sale de tu ingreso variable. Buena señal: tus fijos cubren lo esencial."
    MatrixInsight.BALANCED -> "Tu ingreso fijo cubre lo esencial y lo recreativo. Lo variable queda libre para ahorrar."
}
