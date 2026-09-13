package mx.equilibrio.ui.theme

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
