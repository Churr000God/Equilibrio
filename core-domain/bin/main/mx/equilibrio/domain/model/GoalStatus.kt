package mx.equilibrio.domain.model

enum class GoalStatus {
    /** En progreso; recibe abonos normalmente. */
    ACTIVE,
    /** Alcanzó o superó `targetCents`; ver [Goal.isCompleted], que también la deriva del monto. */
    COMPLETED,
    /** Archivada por el usuario sin haberse completado; deja de contar en resúmenes activos. */
    ARCHIVED,
}
