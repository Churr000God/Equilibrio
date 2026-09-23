package mx.equilibrio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta completa de un tema. Los componentes de `:core-ui` solo leen de
 * aquí — ningún `Color` crudo cruza la frontera del módulo (§1.5 del plan).
 */
data class EquilibrioColors(
    val background: Color,
    val surface: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val border: Color,
    val borderStrong: Color,
    val neutralBadge: Color,
    val green: Color,
    val greenDeep: Color,
    val greenMid: Color,
    val greenSoft: Color,
    val purple: Color,
    val purpleDeep: Color,
    val purpleMid: Color,
    val purpleSoft: Color,
    val error: Color,
    val errorDeep: Color,
    val errorSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val info: Color,
    val infoSoft: Color,
    /** Texto/ícono sobre un gradiente de dominio oscuro (p. ej. [mx.equilibrio.ui.components.EqAccountCard]) — mismo blanco en claro y oscuro. */
    val onGradient: Color,
)

val LightEquilibrioColors = EquilibrioColors(
    background = Background,
    surface = Surface,
    ink = Ink,
    inkMuted = InkMuted,
    inkFaint = InkFaint,
    border = Border,
    borderStrong = BorderStrong,
    neutralBadge = NeutralBadge,
    green = Green,
    greenDeep = GreenDeep,
    greenMid = GreenMid,
    greenSoft = GreenSoft,
    purple = Purple,
    purpleDeep = PurpleDeep,
    purpleMid = PurpleMid,
    purpleSoft = PurpleSoft,
    error = Error,
    errorDeep = ErrorDeep,
    errorSoft = ErrorSoft,
    warning = Warning,
    warningSoft = WarningSoft,
    info = Info,
    infoSoft = InfoSoft,
    onGradient = Color.White,
)

val DarkEquilibrioColors = EquilibrioColors(
    background = BackgroundDark,
    surface = SurfaceDark,
    ink = InkDark,
    inkMuted = InkMutedDark,
    inkFaint = InkFaintDark,
    border = BorderDark,
    borderStrong = BorderStrongDark,
    neutralBadge = NeutralBadgeDark,
    green = GreenDark,
    greenDeep = GreenDeepDark,
    greenMid = GreenMidDark,
    greenSoft = GreenSoftDark,
    purple = PurpleDark,
    purpleDeep = PurpleDeepDark,
    purpleMid = PurpleMidDark,
    purpleSoft = PurpleSoftDark,
    error = ErrorDark,
    errorDeep = ErrorDeepDark,
    errorSoft = ErrorSoftDark,
    warning = WarningDark,
    warningSoft = WarningSoftDark,
    info = InfoDark,
    infoSoft = InfoSoftDark,
    onGradient = Color.White,
)

val LocalEquilibrioColors: ProvidableCompositionLocal<EquilibrioColors> =
    staticCompositionLocalOf { LightEquilibrioColors }

val LocalEquilibrioTypography: ProvidableCompositionLocal<EquilibrioTypography> =
    staticCompositionLocalOf { EquilibrioTypographyDefaults }

/** Refuerza con ícono, además de color, los componentes que dependen de purple-vs-green (ver docs/identidad). */
val LocalAccessibilityMode: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf { false }

object EquilibrioTheme {
    val colors: EquilibrioColors
        @Composable get() = LocalEquilibrioColors.current

    val typography: EquilibrioTypography
        @Composable get() = LocalEquilibrioTypography.current

    val accessibilityMode: Boolean
        @Composable get() = LocalAccessibilityMode.current
}

/**
 * Puentea nuestros tokens a `MaterialTheme.colorScheme` para que los
 * componentes M3 crudos que seguimos usando (TextButton, DatePicker,
 * AlertDialog, Scaffold) hereden la marca en vez del morado por defecto de
 * Material — ese morado no es un color de dominio y no debe aparecer.
 * `secondary` se mantiene en la familia verde a propósito: el morado de
 * marca vive solo en los componentes de dominio (`EqBadge`, `EqAmount`,
 * `EqDomainToggleOption`), nunca en un control neutro del sistema.
 */
private fun EquilibrioColors.toMaterialColorScheme(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = green,
        onPrimary = Color.Black,
        primaryContainer = greenSoft,
        onPrimaryContainer = greenDeep,
        secondary = greenMid,
        onSecondary = Color.Black,
        error = error,
        onError = Color.Black,
        background = background,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = neutralBadge,
        onSurfaceVariant = inkMuted,
        outline = border,
        outlineVariant = borderStrong,
    )
} else {
    lightColorScheme(
        primary = green,
        onPrimary = Color.White,
        primaryContainer = greenSoft,
        onPrimaryContainer = greenDeep,
        secondary = greenMid,
        onSecondary = Color.White,
        error = error,
        onError = Color.White,
        background = background,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = neutralBadge,
        onSurfaceVariant = inkMuted,
        outline = border,
        outlineVariant = borderStrong,
    )
}

@Composable
fun EquilibrioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibilityMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkEquilibrioColors else LightEquilibrioColors
    CompositionLocalProvider(
        LocalEquilibrioColors provides colors,
        LocalEquilibrioTypography provides EquilibrioTypographyDefaults,
        LocalAccessibilityMode provides accessibilityMode,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(darkTheme),
            content = content,
        )
    }
}
