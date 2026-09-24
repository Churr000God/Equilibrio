package mx.equilibrio.domain.model

enum class TransactionStatus {
    /** Ya impactó el balance disponible; no puede tener fecha futura (ver [requireNoFutureConfirmedDate]). */
    COMPLETED,
    /** Fecha futura; todavía no impacta el balance disponible. */
    SCHEDULED,
}
