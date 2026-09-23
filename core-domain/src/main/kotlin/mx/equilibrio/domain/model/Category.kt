package mx.equilibrio.domain.model

data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val type: CategoryType,
    val colorSlot: Int,
    val icon: String,
    val isSystem: Boolean = false,
    val sortOrder: Int,
    val monthlyBudgetCents: Long? = null,
)
