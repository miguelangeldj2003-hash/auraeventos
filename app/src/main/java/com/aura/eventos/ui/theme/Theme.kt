package com.aura.eventos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.eventos.R

// Paleta tomada de las vistas web de Aura Eventos
val AuraNavy = Color(0xFF0B1B32)
val AuraNavySoft = Color(0xFF14283F)
val AuraGreen = Color(0xFF12B88F)
val AuraGreenDark = Color(0xFF0E9D7A)
val AuraGreenSoft = Color(0xFFE3F6F0)
val AuraBg = Color(0xFFF1F5F8)
val AuraBorder = Color(0xFFE3E9F0)
val AuraGold = Color(0xFFC9A24D)
val AuraGoldSoft = Color(0xFFFBF3DE)
val AuraAmber = Color(0xFFB7791F)
val AuraAmberSoft = Color(0xFFFDF1D6)
val AuraAmberBright = Color(0xFFF2A61D)
val AuraRed = Color(0xFFD64545)
val AuraRedSoft = Color(0xFFFBE9E9)
val AuraMuted = Color(0xFF6B778C)
val AuraFaint = Color(0xFF9AA6B8)

// Noto Sans (la misma tipografía de la web); una sola fuente variable con distintos pesos
@OptIn(ExperimentalTextApi::class)
val NotoSans = FontFamily(
    Font(R.font.noto_sans, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.noto_sans, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.noto_sans, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.noto_sans, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.noto_sans, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)

private fun t(size: Int, weight: FontWeight, line: Int = 0, spacing: Float = 0f) =
    TextStyle(fontFamily = NotoSans, fontSize = size.sp, fontWeight = weight,
        lineHeight = (if (line == 0) size * 1.35 else line.toDouble()).sp, letterSpacing = spacing.sp)

private val AuraTypography = Typography(
    displaySmall = t(30, FontWeight.ExtraBold, 36),
    headlineMedium = t(26, FontWeight.ExtraBold, 32),
    headlineSmall = t(22, FontWeight.Bold, 28),
    titleLarge = t(19, FontWeight.Bold, 26),
    titleMedium = t(16, FontWeight.Bold, 22),
    titleSmall = t(14, FontWeight.Bold, 20),
    bodyLarge = t(15, FontWeight.Normal, 22),
    bodyMedium = t(14, FontWeight.Normal, 20),
    bodySmall = t(12, FontWeight.Normal, 17),
    labelLarge = t(14, FontWeight.SemiBold, 20),
    labelMedium = t(12, FontWeight.SemiBold, 16),
    labelSmall = t(10, FontWeight.Bold, 14, 0.8f)
)

private val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(20.dp), extraLarge = RoundedCornerShape(28.dp)
)

private val scheme = lightColorScheme(
    primary = AuraGreen, onPrimary = Color.White, primaryContainer = AuraGreenSoft, onPrimaryContainer = AuraGreenDark,
    secondary = AuraNavy, onSecondary = Color.White, tertiary = AuraGold,
    background = AuraBg, onBackground = AuraNavy, surface = Color.White, onSurface = AuraNavy,
    surfaceVariant = AuraBg, onSurfaceVariant = AuraMuted, outline = AuraBorder, outlineVariant = AuraBorder,
    error = AuraRed, onError = Color.White, surfaceTint = Color.Transparent,
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color.White, surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White, surfaceContainerHighest = AuraBg
)

@Composable
fun AuraTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = scheme, typography = AuraTypography, shapes = AuraShapes, content = content)
