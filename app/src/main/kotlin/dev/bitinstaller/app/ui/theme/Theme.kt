package dev.bitinstaller.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val DARK_PRIMARY_ARGB = 0xFF5E6AD2
private const val DARK_ON_PRIMARY_ARGB = 0xFFF7F8F8
private const val DARK_PRIMARY_CONTAINER_ARGB = 0xFF191A1B
private const val DARK_ON_PRIMARY_CONTAINER_ARGB = 0xFFDCDDFF
private const val DARK_SECONDARY_ARGB = 0xFF7A7FAD
private const val DARK_SECONDARY_CONTAINER_ARGB = 0xFF0F1011
private const val DARK_ON_SECONDARY_CONTAINER_ARGB = 0xFFD0D6E0
private const val DARK_TERTIARY_ARGB = 0xFF10B981
private const val DARK_BACKGROUND_ARGB = 0xFF050607
private const val DARK_ON_SURFACE_VARIANT_ARGB = 0xFF8A8F98
private const val DARK_OUTLINE_ARGB = 0x14FFFFFF
private const val LIGHT_PRIMARY_ARGB = 0xFF5367A7
private const val LIGHT_ON_PRIMARY_ARGB = 0xFFFFFFFF
private const val LIGHT_PRIMARY_CONTAINER_ARGB = 0xFFDCE2FF
private const val LIGHT_ON_PRIMARY_CONTAINER_ARGB = 0xFF101A3F
private const val LIGHT_SECONDARY_ARGB = 0xFF735D91
private const val LIGHT_SECONDARY_CONTAINER_ARGB = 0xFFF0DBFF
private const val LIGHT_ON_SECONDARY_CONTAINER_ARGB = 0xFF291A37
private const val LIGHT_TERTIARY_ARGB = 0xFF1B8B78
private const val LIGHT_BACKGROUND_ARGB = 0xFFF5F5FD
private const val LIGHT_ON_BACKGROUND_ARGB = 0xFF171A24
private const val LIGHT_SURFACE_ARGB = 0xFFFBFAFF
private const val LIGHT_SURFACE_VARIANT_ARGB = 0xFFE2E6F4
private const val LIGHT_ON_SURFACE_VARIANT_ARGB = 0xFF495164
private const val LIGHT_OUTLINE_ARGB = 0xFF70788C
private const val DISPLAY_LARGE_SIZE_SP = 48
private const val DISPLAY_LARGE_LETTER_SPACING = -1.05f
private const val TITLE_LARGE_LINE_HEIGHT_SP = 28
private const val TITLE_LARGE_LETTER_SPACING = -0.3f
private const val TITLE_MEDIUM_LETTER_SPACING = -0.2f
private const val TITLE_SMALL_LINE_HEIGHT_SP = 22
private const val BODY_LARGE_LINE_HEIGHT_SP = 25
private const val BODY_SMALL_LINE_HEIGHT_SP = 19
private const val LABEL_MEDIUM_SIZE_SP = 12
private const val LABEL_MEDIUM_LINE_HEIGHT_SP = 16

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(DARK_PRIMARY_ARGB),
        onPrimary = Color(DARK_ON_PRIMARY_ARGB),
        primaryContainer = Color(DARK_PRIMARY_CONTAINER_ARGB),
        onPrimaryContainer = Color(DARK_ON_PRIMARY_CONTAINER_ARGB),
        secondary = Color(DARK_SECONDARY_ARGB),
        onSecondary = Color(DARK_ON_PRIMARY_ARGB),
        secondaryContainer = Color(DARK_SECONDARY_CONTAINER_ARGB),
        onSecondaryContainer = Color(DARK_ON_SECONDARY_CONTAINER_ARGB),
        tertiary = Color(DARK_TERTIARY_ARGB),
        onTertiary = Color(DARK_ON_PRIMARY_ARGB),
        background = Color(DARK_BACKGROUND_ARGB),
        onBackground = Color(DARK_ON_PRIMARY_ARGB),
        surface = Color(DARK_SECONDARY_CONTAINER_ARGB),
        onSurface = Color(DARK_ON_PRIMARY_ARGB),
        surfaceVariant = Color(DARK_PRIMARY_CONTAINER_ARGB),
        onSurfaceVariant = Color(DARK_ON_SURFACE_VARIANT_ARGB),
        outline = Color(DARK_OUTLINE_ARGB),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color(LIGHT_PRIMARY_ARGB),
        onPrimary = Color(LIGHT_ON_PRIMARY_ARGB),
        primaryContainer = Color(LIGHT_PRIMARY_CONTAINER_ARGB),
        onPrimaryContainer = Color(LIGHT_ON_PRIMARY_CONTAINER_ARGB),
        secondary = Color(LIGHT_SECONDARY_ARGB),
        onSecondary = Color(LIGHT_ON_PRIMARY_ARGB),
        secondaryContainer = Color(LIGHT_SECONDARY_CONTAINER_ARGB),
        onSecondaryContainer = Color(LIGHT_ON_SECONDARY_CONTAINER_ARGB),
        tertiary = Color(LIGHT_TERTIARY_ARGB),
        onTertiary = Color(LIGHT_ON_PRIMARY_ARGB),
        background = Color(LIGHT_BACKGROUND_ARGB),
        onBackground = Color(LIGHT_ON_BACKGROUND_ARGB),
        surface = Color(LIGHT_SURFACE_ARGB),
        onSurface = Color(LIGHT_ON_BACKGROUND_ARGB),
        surfaceVariant = Color(LIGHT_SURFACE_VARIANT_ARGB),
        onSurfaceVariant = Color(LIGHT_ON_SURFACE_VARIANT_ARGB),
        outline = Color(LIGHT_OUTLINE_ARGB),
    )

private val BaseTypography = Typography()

private val VariableSansSerif =
    FontFamily(
        Font(
            DeviceFontFamilyName("sans-serif"),
            variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Normal.weight)),
        ),
    )

private val BitInstallerTypography =
    Typography(
        displayLarge =
            BaseTypography.displayLarge.copy(
                fontFamily = VariableSansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = DISPLAY_LARGE_SIZE_SP.sp,
                lineHeight = DISPLAY_LARGE_SIZE_SP.sp,
                letterSpacing = DISPLAY_LARGE_LETTER_SPACING.sp,
            ),
        titleLarge =
            BaseTypography.titleLarge.copy(
                fontFamily = VariableSansSerif,
                fontWeight = FontWeight.Normal,
                lineHeight = TITLE_LARGE_LINE_HEIGHT_SP.sp,
                letterSpacing = TITLE_LARGE_LETTER_SPACING.sp,
            ),
        titleMedium =
            BaseTypography.titleMedium.copy(
                fontFamily = VariableSansSerif,
                fontWeight = FontWeight.Medium,
                letterSpacing = TITLE_MEDIUM_LETTER_SPACING.sp,
            ),
        titleSmall =
            BaseTypography.titleSmall.copy(
                fontFamily = VariableSansSerif,
                fontWeight = FontWeight.Medium,
                lineHeight = TITLE_SMALL_LINE_HEIGHT_SP.sp,
            ),
        bodyLarge =
            BaseTypography.bodyLarge.copy(
                fontFamily = VariableSansSerif,
                lineHeight = BODY_LARGE_LINE_HEIGHT_SP.sp,
            ),
        bodyMedium =
            BaseTypography.bodyMedium.copy(
                fontFamily = VariableSansSerif,
                lineHeight = TITLE_SMALL_LINE_HEIGHT_SP.sp,
            ),
        bodySmall =
            BaseTypography.bodySmall.copy(
                fontFamily = VariableSansSerif,
                lineHeight = BODY_SMALL_LINE_HEIGHT_SP.sp,
            ),
        labelLarge =
            BaseTypography.labelLarge.copy(
                fontFamily = VariableSansSerif,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = VariableSansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = LABEL_MEDIUM_SIZE_SP.sp,
                lineHeight = LABEL_MEDIUM_LINE_HEIGHT_SP.sp,
                letterSpacing = 0.sp,
            ),
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BitInstallerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = BitInstallerTypography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
