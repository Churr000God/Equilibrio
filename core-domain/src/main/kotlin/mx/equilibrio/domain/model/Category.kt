package mx.equilibrio.domain.model

/**
 * Categoría de un movimiento. La clave `key` es lo que se persiste; el enum
 * es lo que el dominio y la UI manipulan. `SAVINGS` es especial: la usan los
 * abonos a metas y se excluye de reportes de gasto.
 */
enum class Category(val key: String) {
    FOOD("food"),
    TRANSPORT("transport"),
    GROCERIES("groceries"),
    OUTINGS("outings"),
    SUBSCRIPTIONS("subscriptions"),
    MUSIC("music"),
    SAVINGS("savings"),
    OTHER("other"),
    ;

    companion object {
        fun fromKey(key: String?): Category? = key?.let { k -> entries.firstOrNull { it.key == k } }

        /** Categorías que el usuario puede elegir al registrar un gasto. */
        val selectable: List<Category> get() = entries.filter { it != SAVINGS }
    }
}
