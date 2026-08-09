package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlueColorScheme = lightColorScheme(
    primary = MedicalBlue,
    secondary = SteelBlue,
    tertiary = MedicalGreen,
    background = ClinicalBg,
    surface = ClinicalSurface,
    onPrimary = ClinicalSurface,
    onSecondary = ClinicalSurface,
    onTertiary = ClinicalSurface,
    onBackground = ClinicalOnSurface,
    onSurface = ClinicalOnSurface,
    onSurfaceVariant = ClinicalOnSurfaceVariant,
    outline = ClinicalOutline,
    outlineVariant = ClinicalOutlineVariant,
    error = ClinicalError,
    errorContainer = ClinicalErrorContainer,
    onErrorContainer = ClinicalOnErrorContainer
)

private val GreenColorScheme = lightColorScheme(
    primary = SurgicalGreen,
    secondary = SurgicalTeal,
    tertiary = MedicalGreen,
    background = Color(0xFFF3FBF7),
    surface = ClinicalSurface,
    onPrimary = ClinicalSurface,
    onSecondary = ClinicalSurface,
    onTertiary = ClinicalSurface,
    onBackground = ClinicalOnSurface,
    onSurface = ClinicalOnSurface,
    onSurfaceVariant = ClinicalOnSurfaceVariant,
    outline = ClinicalOutline,
    outlineVariant = Color(0xFFCBD6D0),
    error = ClinicalError,
    errorContainer = ClinicalErrorContainer,
    onErrorContainer = ClinicalOnErrorContainer
)

private val PurpleColorScheme = lightColorScheme(
    primary = OncologyPurple,
    secondary = OncologyViolet,
    tertiary = MedicalGreen,
    background = Color(0xFFFAF6FC),
    surface = ClinicalSurface,
    onPrimary = ClinicalSurface,
    onSecondary = ClinicalSurface,
    onTertiary = ClinicalSurface,
    onBackground = ClinicalOnSurface,
    onSurface = ClinicalOnSurface,
    onSurfaceVariant = ClinicalOnSurfaceVariant,
    outline = ClinicalOutline,
    outlineVariant = Color(0xFFD6CBE0),
    error = ClinicalError,
    errorContainer = ClinicalErrorContainer,
    onErrorContainer = ClinicalOnErrorContainer
)

private val CharcoalColorScheme = darkColorScheme(
    primary = CharcoalPrimary,
    secondary = CharcoalSecondary,
    tertiary = Color(0xFF4CAF50),
    background = CharcoalDark,
    surface = CharcoalSurface,
    onPrimary = CharcoalDark,
    onSecondary = CharcoalDark,
    onTertiary = CharcoalDark,
    onBackground = Color(0xFFE3E2E6),
    onSurface = Color(0xFFE3E2E6),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun MyApplicationTheme(
    theme: String = "BLUE",
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        "GREEN" -> GreenColorScheme
        "PURPLE" -> PurpleColorScheme
        "DARK" -> CharcoalColorScheme
        else -> BlueColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getScaledTypography(fontScale),
        content = content
    )
}
