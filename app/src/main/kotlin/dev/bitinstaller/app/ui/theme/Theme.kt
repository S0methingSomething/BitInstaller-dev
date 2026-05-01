@file:Suppress("MagicNumber")

package dev.bitinstaller.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7170FF),
    onPrimary = Color(0xFFF7F8F8),
    primaryContainer = Color(0xFF1D1F39),
    onPrimaryContainer = Color(0xFFDCDDFF),
    secondary = Color(0xFF7A7FAD),
    onSecondary = Color(0xFFF7F8F8),
    secondaryContainer = Color(0xFF171924),
    onSecondaryContainer = Color(0xFFD0D6E0),
    tertiary = Color(0xFF10B981),
    onTertiary = Color(0xFFF7F8F8),
    background = Color(0xFF08090A),
    onBackground = Color(0xFFF7F8F8),
    surface = Color(0xFF0F1011),
    onSurface = Color(0xFFF7F8F8),
    surfaceVariant = Color(0xFF191A1B),
    onSurfaceVariant = Color(0xFF8A8F98),
    outline = Color(0x14FFFFFF),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5367A7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE2FF),
    onPrimaryContainer = Color(0xFF101A3F),
    secondary = Color(0xFF735D91),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0DBFF),
    onSecondaryContainer = Color(0xFF291A37),
    tertiary = Color(0xFF1B8B78),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F5FD),
    onBackground = Color(0xFF171A24),
    surface = Color(0xFFFBFAFF),
    onSurface = Color(0xFF171A24),
    surfaceVariant = Color(0xFFE2E6F4),
    onSurfaceVariant = Color(0xFF495164),
    outline = Color(0xFF70788C),
)

private val BaseTypography = Typography()

private val BitInstallerTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontSize = 50.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.3).sp,
    ),
    titleLarge = BaseTypography.titleLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = BaseTypography.titleMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp,
    ),
    titleSmall = BaseTypography.titleSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp,
    ),
    bodyLarge = BaseTypography.bodyLarge.copy(
        fontFamily = FontFamily.SansSerif,
        lineHeight = 25.sp,
    ),
    bodyMedium = BaseTypography.bodyMedium.copy(
        fontFamily = FontFamily.SansSerif,
        lineHeight = 22.sp,
    ),
    bodySmall = BaseTypography.bodySmall.copy(
        fontFamily = FontFamily.SansSerif,
        lineHeight = 19.sp,
    ),
    labelLarge = BaseTypography.labelLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
)

@Composable
fun BitInstallerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BitInstallerTypography,
        content = content,
    )
}
