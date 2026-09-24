package mx.equilibrio.feature.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.CreditAvailability
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.ui.components.EqBadge
import mx.equilibrio.ui.components.EqButton
import mx.equilibrio.ui.components.EqCard
import mx.equilibrio.ui.components.EqColorSlotPicker
import mx.equilibrio.ui.components.EqDestructiveDialog
import mx.equilibrio.ui.components.EqDomainToggleOption
import mx.equilibrio.ui.components.EqInlineValidation
import mx.equilibrio.ui.components.EqKeyValueRow
import mx.equilibrio.ui.components.EqKeyValueRowGroup
import mx.equilibrio.ui.components.EqSegmentedControl
import mx.equilibrio.ui.components.EqTextField
import mx.equilibrio.ui.components.EqTopBar
import mx.equilibrio.ui.components.formatCents
import mx.equilibrio.ui.theme.DomainTone
import mx.equilibrio.ui.theme.EquilibrioColors
import mx.equilibrio.ui.theme.EquilibrioTheme
import mx.equilibrio.ui.theme.ShapeSmall
import mx.equilibrio.ui.theme.Spacing
import mx.equilibrio.ui.theme.TouchTarget

/** Slots de color fijos para categorías (mismo criterio que categoryColor en :feature-categories). */
private const val CategoryColorSlotCount = 3

/** (profundo, base, medio) de la familia de dominio de este tipo, espejo de categoryPalette en :feature-categories. */
private fun categoryPalette(type: CategoryType, colors: EquilibrioColors) = when (type) {
    CategoryType.INCOME -> Triple(colors.greenDeep, colors.green, colors.greenMid)
    CategoryType.EXPENSE -> Triple(colors.purpleDeep, colors.purple, colors.purpleMid)
}

private fun categoryColor(type: CategoryType, colorSlot: Int, colors: EquilibrioColors): androidx.compose.ui.graphics.Color {
    val (deep, base, mid) = categoryPalette(type, colors)
    return when (((colorSlot % 3) + 3) % 3) {
        0 -> base
        1 -> deep
        else -> mid
    }
}

@Composable
fun QuickEntryScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuickEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun handleBack() {
        if (state.hasUnsavedInput) showDiscardConfirm = true else onCancel()
    }

    if (showDiscardConfirm) {
        EqDestructiveDialog(
            title = "¿Descartar este movimiento?",
            body = "Perderás lo que llevas capturado.",
            confirmLabel = "Descartar",
            onConfirm = onCancel,
            onDismiss = { showDiscardConfirm = false },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(EquilibrioTheme.colors.background)) {
        EqTopBar(
            title = if (state.isEditing) "Editar movimiento" else "Nuevo movimiento",
            onBack = ::handleBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.base)
                .clipToBounds()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            if (state.isEditing) {
                EqSegmentedControl(
                    options = listOf("Ver", "Editar"),
                    selectedIndex = if (state.viewTab == EntryViewTab.VIEW) 0 else 1,
                    onSelect = { index ->
                        val tab = if (index == 0) EntryViewTab.VIEW else EntryViewTab.EDIT
                        viewModel.onEvent(QuickEntryEvent.ViewTabChanged(tab))
                    },
                )
            }

            if (state.isEditing && state.viewTab == EntryViewTab.VIEW) {
                TransactionViewSection(
                    state = state,
                    onConfirmClicked = { viewModel.onEvent(QuickEntryEvent.ConfirmClicked) },
                )
            } else if (state.isEditing && state.viewTab == EntryViewTab.EDIT) {
                // Restyle solo para editar (mockup de Transacciones) — la creación de abajo no se toca.
                EditFormSection(state = state, viewModel = viewModel)
            } else {
                val hasCreditCard = state.accounts.any { it.type == AccountType.CREDIT_CARD }
                val modeOptions = buildList {
                    add("Gasto" to EntryMode.EXPENSE)
                    add("Ingreso" to EntryMode.INCOME)
                    add("Transferencia" to EntryMode.TRANSFER)
                    if (hasCreditCard) add("Compra con tarjeta" to EntryMode.CREDIT_PURCHASE)
                }

                EqSegmentedControl(
                    options = modeOptions.map { it.first },
                    selectedIndex = modeOptions.indexOfFirst { it.second == state.mode }.coerceAtLeast(0),
                    onSelect = { index -> viewModel.onEvent(QuickEntryEvent.EntryModeChanged(modeOptions[index].second)) },
                )

                Column {
                    EqTextField(
                        value = state.amountInput,
                        onValueChange = { viewModel.onEvent(QuickEntryEvent.AmountChanged(it)) },
                        label = "Monto",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.amountError != null,
                        helperOrError = state.amountError,
                    )
                }

                when (state.mode) {
                    EntryMode.TRANSFER -> TransferAccountsSection(
                        accounts = state.accounts,
                        originId = state.originAccountId,
                        destinationId = state.destinationAccountId,
                        error = state.transferError,
                        onOriginSelected = { viewModel.onEvent(QuickEntryEvent.OriginAccountSelected(it)) },
                        onDestinationSelected = { viewModel.onEvent(QuickEntryEvent.DestinationAccountSelected(it)) },
                    )

                    EntryMode.CREDIT_PURCHASE -> {
                        // Al revés que el resto de los modos: acá el selector SOLO ofrece tarjetas,
                        // porque este es el único camino admitido para gastar contra una CREDIT_CARD
                        // (ver SaveCreditPurchase / SaveTransaction en core-domain).
                        AccountPickerSection(
                            label = "Tarjeta",
                            accounts = state.accounts.filter { it.type == AccountType.CREDIT_CARD },
                            selectedId = state.accountId,
                            onSelect = { viewModel.onEvent(QuickEntryEvent.CreditAccountSelected(it)) },
                        )

                        InstallmentSection(
                            isInstallment = state.isInstallment,
                            installmentCount = state.installmentCount,
                            onToggle = { viewModel.onEvent(QuickEntryEvent.InstallmentToggled(it)) },
                            onCountSelected = { viewModel.onEvent(QuickEntryEvent.InstallmentCountChanged(it)) },
                        )

                        ClassificationSection(
                            kind = state.kind,
                            selected = state.classification,
                            onSelect = { viewModel.onEvent(QuickEntryEvent.ClassificationChanged(it)) },
                        )

                        CategorySection(
                            kind = state.kind,
                            categories = state.categories,
                            selectedId = state.categoryId,
                               isCreating = state.isCreatingCategory,
                            newCategoryName = state.newCategoryName,
                            newCategoryNameError = state.newCategoryNameError,
                            newCategoryColorSlot = state.newCategoryColorSlot,
                            isSavingCategory = state.isSavingCategory,
                            onSelect = { viewModel.onEvent(QuickEntryEvent.CategorySelected(it)) },
                            onToggleCreate = { viewModel.onEvent(QuickEntryEvent.CreateCategoryTabToggled(it)) },
                            onNewNameChanged = { viewModel.onEvent(QuickEntryEvent.NewCategoryNameChanged(it)) },
                            onNewColorSlotChanged = { viewModel.onEvent(QuickEntryEvent.NewCategoryColorSlotChanged(it)) },
                            onSaveCategory = { viewModel.onEvent(QuickEntryEvent.SaveCategoryClicked) },
                        )

                        if (state.isInstallment) {
                            InstallmentPreview(
                                totalCents = state.amountCents,
                                monthlyCents = state.installmentBaseCents,
                                firstOccurredAt = state.occurredAt,
                                lastOccurredAt = state.installmentLastOccurredAt,
                            )
                        }

                        CreditPeriodInfo(
                            period = state.creditPeriod,
                            available = state.accountId?.let { state.creditAvailable[it] },
                        )

                        state.creditLimitError?.let { EqInlineValidation(it) }
                    }

                    EntryMode.EXPENSE, EntryMode.INCOME -> {
                        // Un gasto/ingreso normal no puede apuntar a una CREDIT_CARD (SaveTransaction lo
                        // rechaza): se filtra acá para no dejar elegir una opción que el guardado va a
                        // rechazar después. La compra con tarjeta tiene su propio modo (CREDIT_PURCHASE).
                        AccountPickerSection(
                            label = "Cuenta",
                            accounts = state.accounts.filter { it.type != AccountType.CREDIT_CARD },
                            selectedId = state.accountId,
                            onSelect = { viewModel.onEvent(QuickEntryEvent.AccountSelected(it)) },
                        )

                        ClassificationSection(
                            kind = state.kind,
                            selected = state.classification,
                            onSelect = { viewModel.onEvent(QuickEntryEvent.ClassificationChanged(it)) },
                        )

                        CategorySection(
                            kind = state.kind,
                            categories = state.categories,
                            selectedId = state.categoryId,
                            isCreating = state.isCreatingCategory,
                            newCategoryName = state.newCategoryName,
                            newCategoryNameError = state.newCategoryNameError,
                            newCategoryColorSlot = state.newCategoryColorSlot,
                            isSavingCategory = state.isSavingCategory,
                            onSelect = { viewModel.onEvent(QuickEntryEvent.CategorySelected(it)) },
                            onToggleCreate = { viewModel.onEvent(QuickEntryEvent.CreateCategoryTabToggled(it)) },
                            onNewNameChanged = { viewModel.onEvent(QuickEntryEvent.NewCategoryNameChanged(it)) },
                            onNewColorSlotChanged = { viewModel.onEvent(QuickEntryEvent.NewCategoryColorSlotChanged(it)) },
                            onSaveCategory = { viewModel.onEvent(QuickEntryEvent.SaveCategoryClicked) },
                        )

                        // Convertir una transacción existente en serie recurrente queda fuera de alcance.
                        if (!state.isEditing) {
                            RecurrenceSection(
                                isRecurring = state.isRecurring,
                                frequency = state.recurrenceFrequency,
                                nextAt = state.recurrenceNextAt,
                                onToggle = { viewModel.onEvent(QuickEntryEvent.RecurringToggled(it)) },
                                onFrequencyChanged = { viewModel.onEvent(QuickEntryEvent.RecurrenceFrequencyChanged(it)) },
                            )
                        }
                    }
                }

                DateSection(
                    dateLabel = state.occurredAt.toString(),
                    onDateSelected = { viewModel.onEvent(QuickEntryEvent.DateChanged(it)) },
                    maxDate = if (state.isEditing && state.status == TransactionStatus.COMPLETED) today() else null,
                )
                state.dateError?.let { EqInlineValidation(it) }

                EqTextField(
                    value = state.note,
                    onValueChange = { viewModel.onEvent(QuickEntryEvent.NoteChanged(it)) },
                    label = "Nota (opcional)",
                )

                EqButton(
                    text = when (state.mode) {
                        EntryMode.EXPENSE -> if (state.isRecurring) "Guardar gasto recurrente" else "Guardar gasto"
                        EntryMode.INCOME -> if (state.isRecurring) "Guardar ingreso recurrente" else "Guardar ingreso"
                        EntryMode.TRANSFER -> "Guardar transferencia"
                        EntryMode.CREDIT_PURCHASE -> "Guardar compra"
                    },
                    onClick = { viewModel.onEvent(QuickEntryEvent.SaveClicked) },
                    enabled = state.canSave,
                    loading = state.isSaving,
                    modifier = Modifier.padding(vertical = Spacing.base),
                )
            }
        }
    }
}

@Composable
private fun ClassificationSection(
    kind: TransactionKind,
    selected: Classification?,
    onSelect: (Classification) -> Unit,
) {
    val options = if (kind == TransactionKind.EXPENSE) {
        listOf(
            "Esencial" to Classification.ESSENTIAL,
            "Recreativo" to Classification.RECREATIONAL,
        )
    } else {
        listOf(
            "Fijo" to Classification.FIXED,
            "Variable" to Classification.VARIABLE,
        )
    }
    val tones = if (kind == TransactionKind.EXPENSE) {
        DomainTone.ESSENTIAL to DomainTone.RECREATIONAL
    } else {
        DomainTone.INCOME_FIXED to DomainTone.INCOME_VARIABLE
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = if (kind == TransactionKind.EXPENSE) {
                "¿Este gasto es indispensable o es para disfrutar?"
            } else {
                "¿Este ingreso llega siempre igual o cambia cada mes?"
            },
            style = EquilibrioTheme.typography.bodySmall,
            color = EquilibrioTheme.colors.inkMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            EqDomainToggleOption(
                label = options[0].first,
                tone = tones.first,
                selected = selected == options[0].second,
                onClick = { onSelect(options[0].second) },
                modifier = Modifier.weight(1f),
            )
            EqDomainToggleOption(
                label = options[1].first,
                tone = tones.second,
                selected = selected == options[1].second,
                onClick = { onSelect(options[1].second) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CategorySection(
    kind: TransactionKind,
    categories: List<Category>,
    selectedId: String?,
    isCreating: Boolean,
    newCategoryName: String,
    newCategoryNameError: String?,
    newCategoryColorSlot: Int,
    isSavingCategory: Boolean,
    onSelect: (String?) -> Unit,
    onToggleCreate: (Boolean) -> Unit,
    onNewNameChanged: (String) -> Unit,
    onNewColorSlotChanged: (Int) -> Unit,
    onSaveCategory: () -> Unit,
) {
    val type = if (kind == TransactionKind.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
    val tone = if (kind == TransactionKind.EXPENSE) DomainTone.RECREATIONAL else DomainTone.INCOME_FIXED
    val filtered = categories.filter { it.type == type }.sortedBy { it.name }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = "Categoría (opcional)",
            style = EquilibrioTheme.typography.bodySmall,
            color = EquilibrioTheme.colors.inkMuted,
        )

        EqSegmentedControl(
            options = listOf("Elegir", "Nueva categoría"),
            selectedIndex = if (isCreating) 1 else 0,
            onSelect = { index -> onToggleCreate(index == 1) },
        )

        if (isCreating) {
            EqTextField(
                value = newCategoryName,
                onValueChange = onNewNameChanged,
                label = "Nombre de la categoría",
                isError = newCategoryNameError != null,
                helperOrError = newCategoryNameError,
            )
            EqColorSlotPicker(
                colors = (0 until CategoryColorSlotCount).map { slot ->
                    categoryColor(type, slot, EquilibrioTheme.colors)
                },
                selectedSlot = newCategoryColorSlot,
                onSelect = onNewColorSlotChanged,
            )
            EqButton(
                text = "Guardar categoría",
                onClick = onSaveCategory,
                enabled = newCategoryName.isNotBlank() && !isSavingCategory,
                loading = isSavingCategory,
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                EqDomainToggleOption(
                    label = "Sin categoría",
                    tone = DomainTone.NEUTRAL,
                    selected = selectedId == null,
                    onClick = { onSelect(null) },
                )
                filtered.forEach { category ->
                    EqDomainToggleOption(
                        label = category.name,
                        tone = tone,
                        selected = selectedId == category.id,
                        onClick = { onSelect(category.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferAccountsSection(
    accounts: List<Account>,
    originId: String?,
    destinationId: String?,
    error: String?,
    onOriginSelected: (String) -> Unit,
    onDestinationSelected: (String) -> Unit,
) {
    // CreateTransfer solo admite cuentas CASH/BANK en ambos extremos; una tarjeta no participa
    // de una transferencia normal (ver CreateTransfer en core-domain).
    val transferable = accounts.filter { it.type != AccountType.CREDIT_CARD }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                text = "Cuenta origen",
                style = EquilibrioTheme.typography.bodySmall,
                color = EquilibrioTheme.colors.inkMuted,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                transferable.forEach { account ->
                    EqDomainToggleOption(
                        label = account.name,
                        tone = DomainTone.NEUTRAL,
                        selected = originId == account.id,
                        onClick = { onOriginSelected(account.id) },
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                text = "Cuenta destino",
                style = EquilibrioTheme.typography.bodySmall,
                color = EquilibrioTheme.colors.inkMuted,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                transferable.forEach { account ->
                    EqDomainToggleOption(
                        label = account.name,
                        tone = DomainTone.NEUTRAL,
                        selected = destinationId == account.id,
                        onClick = { onDestinationSelected(account.id) },
                    )
                }
            }
        }

        if (error != null) {
            EqInlineValidation(error)
        }
    }
}

@Composable
private fun AccountPickerSection(
    label: String,
    accounts: List<Account>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = label,
            style = EquilibrioTheme.typography.bodySmall,
            color = EquilibrioTheme.colors.inkMuted,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            accounts.forEach { account ->
                EqDomainToggleOption(
                    label = account.name,
                    tone = DomainTone.NEUTRAL,
                    selected = selectedId == account.id,
                    onClick = { onSelect(account.id) },
                )
            }
        }
    }
}

private val InstallmentCountOptions = listOf(3, 6, 9, 12, 18, 24)

@Composable
private fun InstallmentSection(
    isInstallment: Boolean,
    installmentCount: Int,
    onToggle: (Boolean) -> Unit,
    onCountSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        EqSegmentedControl(
            options = listOf("Un pago", "A meses"),
            selectedIndex = if (isInstallment) 1 else 0,
            onSelect = { index -> onToggle(index == 1) },
        )
        if (isInstallment) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                InstallmentCountOptions.forEach { count ->
                    EqDomainToggleOption(
                        label = "${count}x",
                        tone = DomainTone.NEUTRAL,
                        selected = installmentCount == count,
                        onClick = { onCountSelected(count) },
                    )
                }
            }
        }
    }
}

private val RECURRENCE_OPTIONS = listOf(
    "Semanal" to RecurrenceFrequency.WEEKLY,
    "Quincenal" to RecurrenceFrequency.BIWEEKLY,
    "Mensual" to RecurrenceFrequency.MONTHLY,
)

@Composable
private fun RecurrenceSection(
    isRecurring: Boolean,
    frequency: RecurrenceFrequency,
    nextAt: kotlinx.datetime.LocalDate,
    onToggle: (Boolean) -> Unit,
    onFrequencyChanged: (RecurrenceFrequency) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        EqSegmentedControl(
            options = listOf("Una vez", "Se repite"),
            selectedIndex = if (isRecurring) 1 else 0,
            onSelect = { index -> onToggle(index == 1) },
        )
        if (isRecurring) {
            EqSegmentedControl(
                options = RECURRENCE_OPTIONS.map { it.first },
                selectedIndex = RECURRENCE_OPTIONS.indexOfFirst { it.second == frequency },
                onSelect = { index -> onFrequencyChanged(RECURRENCE_OPTIONS[index].second) },
            )
            Text(
                text = "Próxima ocurrencia: $nextAt",
                style = EquilibrioTheme.typography.bodySmall,
                color = EquilibrioTheme.colors.inkMuted,
            )
        }
    }
}

@Composable
private fun InstallmentPreview(
    totalCents: Long,
    monthlyCents: Long,
    firstOccurredAt: kotlinx.datetime.LocalDate,
    lastOccurredAt: kotlinx.datetime.LocalDate,
) {
    EqCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            EqKeyValueRowGroup(
                items = listOf(
                    "Cuota mensual" to formatCents(monthlyCents),
                    "Total" to formatCents(totalCents),
                ),
            )
            EqKeyValueRowGroup(
                items = listOf(
                    "Primera cuota" to firstOccurredAt.toString(),
                    "Última cuota" to lastOccurredAt.toString(),
                ),
            )
        }
    }
}

private fun periodStateLabel(state: PeriodState): String = when (state) {
    PeriodState.OPEN -> "Periodo abierto"
    PeriodState.AWAITING_PAYMENT -> "Esperando pago"
    PeriodState.CLOSED -> "Cerrado"
}

@Composable
private fun CreditPeriodInfo(period: Period?, available: CreditAvailability?) {
    if (period == null && available == null) return
    val colors = EquilibrioTheme.colors

    EqCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            period?.let {
                Text(
                    text = periodStateLabel(it.state),
                    style = EquilibrioTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Text(
                    text = "Corte: ${it.endAt} · Pago límite: ${it.payAt}",
                    style = EquilibrioTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            available?.let {
                EqKeyValueRow(
                    label = "Disponible",
                    value = formatCents(it.realCents),
                    valueColor = if (it.realCents < 0) colors.error else colors.ink,
                    modifier = Modifier.padding(top = if (period != null) Spacing.xs else 0.dp),
                )
                EqKeyValueRow(
                    label = "Disponible proyectado",
                    value = formatCents(it.projectedCents),
                    valueColor = if (it.projectedCents < 0) colors.error else colors.ink,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

/**
 * Restyle de edición del mockup de Transacciones: filas compactas (ícono+etiqueta+valor+chevron)
 * en vez del formulario expandido de creación — cada una abre su propio diálogo. Sin sección de
 * "Compra en cuotas": el botón Editar ya está oculto para cuotas en TransactionDetailScreen, este
 * caso no es alcanzable acá. "Recurrente" sí es alcanzable (una ocurrencia SÍ es editable) y se
 * muestra solo informativo, nunca interactivo — convertir/desconvertir la serie queda fuera de alcance.
 */
@Composable
private fun EditFormSection(state: QuickEntryUiState, viewModel: QuickEntryViewModel) {
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val accountName = state.accounts.firstOrNull { it.id == state.accountId }?.name ?: "Elegir cuenta"
    val categoryName = state.categories.firstOrNull { it.id == state.categoryId }?.name ?: "Sin categoría"

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        EqSegmentedControl(
            options = listOf("Gasto", "Ingreso"),
            selectedIndex = if (state.mode == EntryMode.EXPENSE) 0 else 1,
            onSelect = { index ->
                viewModel.onEvent(QuickEntryEvent.EntryModeChanged(if (index == 0) EntryMode.EXPENSE else EntryMode.INCOME))
            },
        )

        EqTextField(
            value = state.amountInput,
            onValueChange = { viewModel.onEvent(QuickEntryEvent.AmountChanged(it)) },
            label = "Monto",
            keyboardType = KeyboardType.Decimal,
            isError = state.amountError != null,
            helperOrError = state.amountError,
        )

        ClassificationSection(
            kind = state.kind,
            selected = state.classification,
            onSelect = { viewModel.onEvent(QuickEntryEvent.ClassificationChanged(it)) },
        )

        Column {
            EditRow(
                icon = Icons.Rounded.Notes,
                label = "Descripción",
                value = state.note.ifBlank { "Agregar descripción" },
                onClick = { showDescriptionDialog = true },
            )
            EditRow(
                icon = Icons.Rounded.Category,
                label = "Categoría",
                value = categoryName,
                onClick = { showCategoryDialog = true },
            )
            EditRow(
                icon = Icons.Rounded.AccountBalanceWallet,
                label = "Cuenta",
                value = accountName,
                onClick = { showAccountDialog = true },
            )
            EditRow(
                icon = Icons.Rounded.CalendarToday,
                label = "Fecha",
                value = state.occurredAt.toString(),
                onClick = { showDatePicker = true },
            )
        }

        ConfirmedToggle(
            isConfirmed = state.status == TransactionStatus.COMPLETED,
            canConfirm = state.occurredAt <= today(),
            isConfirming = state.isConfirming,
            onConfirmClicked = { viewModel.onEvent(QuickEntryEvent.ConfirmClicked) },
        )
        state.confirmError?.let { EqInlineValidation(it) }

        if (state.editingIsRecurringOccurrence) {
            Text(
                text = "Recurrente — esto solo cambia este movimiento, no la serie.",
                style = EquilibrioTheme.typography.caption,
                color = EquilibrioTheme.colors.inkMuted,
            )
        }

        EqButton(
            text = "Guardar cambios",
            onClick = { viewModel.onEvent(QuickEntryEvent.SaveClicked) },
            enabled = state.canSave,
            loading = state.isSaving,
            modifier = Modifier.padding(vertical = Spacing.base),
        )
    }

    if (showDescriptionDialog) {
        var draft by remember(state.note) { mutableStateOf(state.note) }
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("Descripción") },
            text = {
                EqTextField(value = draft, onValueChange = { draft = it }, label = "Descripción")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(QuickEntryEvent.NoteChanged(draft))
                    showDescriptionDialog = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDescriptionDialog = false }) { Text("Cancelar") }
            },
        )
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Categoría") },
            text = {
                CategoryPickerList(
                    kind = state.kind,
                    categories = state.categories,
                    selectedId = state.categoryId,
                    onSelect = {
                        viewModel.onEvent(QuickEntryEvent.CategorySelected(it))
                        showCategoryDialog = false
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text("Cerrar") }
            },
        )
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Cuenta") },
            text = {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    // Editar acá siempre es un gasto/ingreso normal (las cuotas no llegan a este form,
                    // ver KDoc de EditFormSection), así que aplica la misma exclusión de CREDIT_CARD.
                    state.accounts.filter { it.type != AccountType.CREDIT_CARD }.forEach { account ->
                        EqDomainToggleOption(
                            label = account.name,
                            tone = DomainTone.NEUTRAL,
                            selected = state.accountId == account.id,
                            onClick = {
                                viewModel.onEvent(QuickEntryEvent.AccountSelected(account.id))
                                showAccountDialog = false
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDialog = false }) { Text("Cerrar") }
            },
        )
    }

    if (showDatePicker) {
        val maxDate = if (state.status == TransactionStatus.COMPLETED) today() else null
        val selectableDates = remember(maxDate) {
            if (maxDate == null) {
                androidx.compose.material3.DatePickerDefaults.AllDates
            } else {
                object : androidx.compose.material3.SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date <= maxDate
                }
            }
        }
        val pickerState = rememberDatePickerState(selectableDates = selectableDates)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.onEvent(
                            QuickEntryEvent.DateChanged(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date),
                        )
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun EditRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit) {
    val colors = EquilibrioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.minRowHeight)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(icon, contentDescription = null, tint = colors.inkMuted)
        Text(text = label, style = EquilibrioTheme.typography.body, color = colors.ink, modifier = Modifier.weight(1f))
        Text(text = value, style = EquilibrioTheme.typography.bodyStrong, color = colors.inkMuted)
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.inkFaint)
    }
}

@Composable
private fun CategoryPickerList(
    kind: TransactionKind,
    categories: List<Category>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    val type = if (kind == TransactionKind.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
    val tone = if (kind == TransactionKind.EXPENSE) DomainTone.RECREATIONAL else DomainTone.INCOME_FIXED
    val filtered = categories.filter { it.type == type }.sortedBy { it.name }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        EqDomainToggleOption(
            label = "Sin categoría",
            tone = DomainTone.NEUTRAL,
            selected = selectedId == null,
            onClick = { onSelect(null) },
        )
        filtered.forEach { category ->
            EqDomainToggleOption(
                label = category.name,
                tone = tone,
                selected = selectedId == category.id,
                onClick = { onSelect(category.id) },
            )
        }
    }
}

@Composable
private fun ConfirmedToggle(
    isConfirmed: Boolean,
    canConfirm: Boolean,
    isConfirming: Boolean,
    onConfirmClicked: () -> Unit,
) {
    val colors = EquilibrioTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Confirmada", style = EquilibrioTheme.typography.body, color = colors.ink)
            Switch(
                checked = isConfirmed,
                onCheckedChange = { if (!isConfirmed) onConfirmClicked() },
                enabled = !isConfirmed && canConfirm && !isConfirming,
            )
        }
        if (!isConfirmed && !canConfirm) {
            Text(
                text = "No se puede confirmar con fecha futura",
                style = EquilibrioTheme.typography.caption,
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun TransactionViewSection(
    state: QuickEntryUiState,
    onConfirmClicked: () -> Unit,
) {
    val accountName = state.accounts.firstOrNull { it.id == state.accountId }?.name ?: "—"
    val categoryName = state.categories.firstOrNull { it.id == state.categoryId }?.name ?: "Sin categoría"

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        if (state.status == TransactionStatus.SCHEDULED) {
            EqBadge(text = "Programada", tone = DomainTone.NEUTRAL)
        }

        SummaryRow(label = "Monto", value = state.amountInput)
        SummaryRow(label = "Cuenta", value = accountName)
        SummaryRow(label = "Categoría", value = categoryName)
        SummaryRow(label = "Fecha", value = state.occurredAt.toString())
        if (state.note.isNotBlank()) {
            SummaryRow(label = "Nota", value = state.note)
        }

        if (state.status == TransactionStatus.SCHEDULED) {
            val canConfirm = state.occurredAt <= today()
            EqButton(
                text = "Confirmar",
                onClick = onConfirmClicked,
                enabled = canConfirm,
                loading = state.isConfirming,
                modifier = Modifier.padding(top = Spacing.base),
            )
            if (!canConfirm) {
                Text(
                    text = "No se puede confirmar con fecha futura",
                    style = EquilibrioTheme.typography.caption,
                    color = EquilibrioTheme.colors.inkMuted,
                )
            }
            state.confirmError?.let { EqInlineValidation(it) }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(text = label, style = EquilibrioTheme.typography.bodySmall, color = EquilibrioTheme.colors.inkMuted)
        Text(text = value, style = EquilibrioTheme.typography.bodyStrong, color = EquilibrioTheme.colors.ink)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSection(
    dateLabel: String,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit,
    maxDate: kotlinx.datetime.LocalDate? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showPicker = true
        }
    }

    val colors = EquilibrioTheme.colors
    OutlinedTextField(
        value = dateLabel,
        onValueChange = {},
        readOnly = true,
        label = { Text("Fecha de ejecución") },
        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        singleLine = true,
        shape = ShapeSmall,
        interactionSource = interactionSource,
        textStyle = EquilibrioTheme.typography.body,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.info,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
            cursorColor = colors.info,
        ),
    )

    if (showPicker) {
        val selectableDates = remember(maxDate) {
            if (maxDate == null) {
                androidx.compose.material3.DatePickerDefaults.AllDates
            } else {
                object : androidx.compose.material3.SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.fromEpochMilliseconds(utcTimeMillis)
                            .toLocalDateTime(TimeZone.UTC).date
                        return date <= maxDate
                    }
                }
            }
        }
        val pickerState = rememberDatePickerState(selectableDates = selectableDates)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(date)
                    }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
