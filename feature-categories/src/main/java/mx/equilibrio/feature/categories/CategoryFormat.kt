package mx.equilibrio.feature.categories

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.YearMonth

private val MONTHS = listOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic",
)

private val FULL_MONTHS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

/** "22 sep" — mismo criterio compacto que el mock (designs/categorias.png). */
fun LocalDate.shortDateLabel(): String = "$dayOfMonth ${MONTHS[monthNumber - 1]}"

/** "Septiembre 2026". */
fun YearMonth.label(): String = "${FULL_MONTHS[month - 1].replaceFirstChar { it.uppercase() }} $year"
