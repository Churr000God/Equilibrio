package mx.equilibrio.domain.model

enum class PeriodState {
    /** Ciclo en curso: todavía admite cargos nuevos. */
    OPEN,
    /** El corte ya pasó; solo falta que se registre el pago antes de `payAt`. */
    AWAITING_PAYMENT,
    /** Ya se pagó (o se dio por saldado); no participa en el disponible activo. */
    CLOSED,
}
