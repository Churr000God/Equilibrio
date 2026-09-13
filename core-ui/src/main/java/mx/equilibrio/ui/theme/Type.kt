package mx.equilibrio.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import mx.equilibrio.ui.R

/**
 * Ambas familias son fuentes variables (un solo archivo, eje `wght` — Inter
 * también trae `opsz`). Cada peso se instancia fijando la variación en vez
 * de cargar un archivo estático por peso.
 */
@OptIn(ExperimentalTextApi::class)
private fun plusJakartaSans(weight: FontWeight) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: FontWeight, opticalSize: Float = 14f) = Font(
    resId = R.font.inter,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.Setting("opsz", opticalSize),
        FontVariation.weight(weight.weight),
    ),
)

val PlusJakartaSans = FontFamily(
    plusJakartaSans(FontWeight.Medium),
    plusJakartaSans(FontWeight.SemiBold),
    plusJakartaSans(FontWeight.Bold),
    plusJakartaSans(FontWeight.ExtraBold),
)

val Inter = FontFamily(
    inter(FontWeight.Normal),
    inter(FontWeight.Medium),
    inter(FontWeight.SemiBold),
)

/** Cifras tabulares: todo monto o porcentaje las usa, nunca Inter (§06). */
private const val TNUM = "\"tnum\" 1"

data class EquilibrioTypography(
    val displayCifra: TextStyle,
    val displayCifraSmall: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
)

val EquilibrioTypographyDefaults = EquilibrioTypography(
    displayCifra = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = TNUM,
    ),
    displayCifraSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.01).em,
        fontFeatureSettings = TNUM,
    ),
    h1 = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.01).em,
    ),
    h2 = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    h3 = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    body = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyStrong = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    label = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.01.em,
    ),
    caption = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.02.em,
    ),
)

/** Estilo tabular para cifras a un tamaño arbitrario (`EqAmount` lo aplica siempre). */
fun tabularAmountStyle(fontSize: TextUnit, weight: FontWeight = FontWeight.Bold): TextStyle = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = weight,
    fontSize = fontSize,
    fontFeatureSettings = TNUM,
)
