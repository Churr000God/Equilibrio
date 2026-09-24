package mx.equilibrio.domain.calc

import mx.equilibrio.domain.model.report.CrossMatrix
import mx.equilibrio.domain.model.report.MatrixInsight
import mx.equilibrio.domain.model.report.PeriodTotals

/**
 * Asigna cada tipo de gasto a un tipo de ingreso siguiendo la tesis del
 * producto: el ingreso fijo sostiene primero lo esencial; lo que sobra del
 * fijo cubre recreativo; el variable paga lo que falte, esencial antes que
 * recreativo. Determinista y sin estado — testeable sin instrumentación.
 */
object CrossMatrixCalculator {

    fun compute(totals: PeriodTotals): CrossMatrix {
        val fixed = totals.fixedIncomeCents
        val variable = totals.variableIncomeCents
        val essential = totals.essentialExpenseCents
        val recreational = totals.recreationalExpenseCents

        val fixedEssential = minOf(fixed, essential)
        val fixedRecreational = minOf(fixed - fixedEssential, recreational)
        val variableEssential = minOf(variable, essential - fixedEssential)
        val variableRecreational = minOf(variable - variableEssential, recreational - fixedRecreational)

        return CrossMatrix(
            fixedEssential = fixedEssential,
            fixedRecreational = fixedRecreational,
            variableEssential = variableEssential,
            variableRecreational = variableRecreational,
            incomeCents = fixed + variable,
        )
    }

    fun insight(totals: PeriodTotals): MatrixInsight {
        val income = totals.fixedIncomeCents + totals.variableIncomeCents
        val expense = totals.essentialExpenseCents + totals.recreationalExpenseCents
        return when {
            // Caso sin datos: no hubo ingreso ni gasto clasificado en el periodo
            income == 0L && expense == 0L -> MatrixInsight.NO_DATA
            // Caso sin ingreso: hay gasto pero nada contra qué cruzarlo
            income == 0L -> MatrixInsight.NO_INCOME
            // Caso de sobregasto: el gasto total superó lo que entró
            expense > income -> MatrixInsight.OVERSPENT
            // Caso de esencial descubierto: el fijo no alcanza ni para lo esencial
            totals.essentialExpenseCents > totals.fixedIncomeCents -> MatrixInsight.ESSENTIAL_UNCOVERED
            else -> {
                val matrix = compute(totals)
                if (matrix.variableRecreational > matrix.fixedRecreational) {
                    // Caso recreativo financiado con variable: se gastó más recreativo del ingreso variable que del fijo
                    MatrixInsight.RECREATIONAL_FROM_VARIABLE
                } else {
                    MatrixInsight.BALANCED
                }
            }
        }
    }
}
