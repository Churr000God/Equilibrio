package mx.equilibrio.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colores de DOMINIO — clasifican un movimiento. Nunca aparecen en un
 * mensaje del sistema (invariante I1 del manual de identidad).
 */
enum class DomainTone { INCOME_FIXED, INCOME_VARIABLE, ESSENTIAL, RECREATIONAL, NEUTRAL }

/**
 * Colores de ESTADO — describen la confianza del sistema en una acción.
 * Nunca rellenan un chip, insignia, monto o segmento de gráfica (invariante I2).
 */
enum class FeedbackTone { SUCCESS, WARNING, ERROR, INFO }

/** Tono base: relleno sólido, trazo de anillo o barra, color de monto. */
fun DomainTone.color(colors: EquilibrioColors): Color = when (this) {
    DomainTone.INCOME_FIXED -> colors.greenDeep
    DomainTone.INCOME_VARIABLE -> colors.greenMid
    DomainTone.ESSENTIAL -> colors.green
    DomainTone.RECREATIONAL -> colors.purple
    DomainTone.NEUTRAL -> colors.inkMuted
}

/** Tono profundo: texto sobre relleno suave (nunca el tono base — contraste AA). */
fun DomainTone.deepColor(colors: EquilibrioColors): Color = when (this) {
    DomainTone.INCOME_FIXED, DomainTone.INCOME_VARIABLE, DomainTone.ESSENTIAL -> colors.greenDeep
    DomainTone.RECREATIONAL -> colors.purpleDeep
    DomainTone.NEUTRAL -> colors.inkMuted
}

/** Relleno suave: fondo de celda, tile o pista. */
fun DomainTone.softColor(colors: EquilibrioColors): Color = when (this) {
    DomainTone.INCOME_FIXED, DomainTone.INCOME_VARIABLE, DomainTone.ESSENTIAL -> colors.greenSoft
    DomainTone.RECREATIONAL -> colors.purpleSoft
    DomainTone.NEUTRAL -> colors.neutralBadge
}
