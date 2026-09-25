package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class EmergencyColorScheme(
    val bluePrimary: Color,
    val blueDark: Color,
    val blueLight: Color,
    val blueContainer: Color,
    val redCritical: Color,
    val redPulse: Color,
    val redContainer: Color,
    val magentaTactical: Color,
    val magentaDark: Color,
    val magentaElectric: Color,
    val magentaContainer: Color,
    val orangeWarning: Color,
    val orangeBright: Color,
    val orangeContainer: Color,
    val greenStable: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val surfaceBorder: Color
)

val LocalEmergencyColors = staticCompositionLocalOf {
    EmergencyColorScheme(
        bluePrimary = SurakshaBluePrimary,
        blueDark = SurakshaBlueDark,
        blueLight = SurakshaBlueLight,
        blueContainer = SurakshaBlueContainerDark,
        redCritical = EmergencyRedCritical,
        redPulse = EmergencyRedPulse,
        redContainer = EmergencyRedContainerDark,
        magentaTactical = TacticalMagenta,
        magentaDark = TacticalMagentaDark,
        magentaElectric = TacticalMagentaElectric,
        magentaContainer = TacticalMagentaContainerDark,
        orangeWarning = WarningOrange,
        orangeBright = WarningOrangeBright,
        orangeContainer = WarningOrangeContainerDark,
        greenStable = EmergencyGreenStable,
        textPrimary = TextPrimaryDark,
        textSecondary = TextSecondaryDark,
        surfaceBorder = SurfaceBorderDark
    )
}

private val DarkEmergencyColors = EmergencyColorScheme(
    bluePrimary = SurakshaBluePrimary,
    blueDark = SurakshaBlueDark,
    blueLight = SurakshaBlueLight,
    blueContainer = SurakshaBlueContainerDark,
    redCritical = EmergencyRedCritical,
    redPulse = EmergencyRedPulse,
    redContainer = EmergencyRedContainerDark,
    magentaTactical = TacticalMagenta,
    magentaDark = TacticalMagentaDark,
    magentaElectric = TacticalMagentaElectric,
    magentaContainer = TacticalMagentaContainerDark,
    orangeWarning = WarningOrange,
    orangeBright = WarningOrangeBright,
    orangeContainer = WarningOrangeContainerDark,
    greenStable = EmergencyGreenStable,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    surfaceBorder = SurfaceBorderDark
)

private val LightEmergencyColors = EmergencyColorScheme(
    bluePrimary = SurakshaBluePrimary,
    blueDark = SurakshaBlueDark,
    blueLight = SurakshaBlueLight,
    blueContainer = SurakshaBlueContainerLight,
    redCritical = EmergencyRedCritical,
    redPulse = EmergencyRedPulse,
    redContainer = EmergencyRedContainerLight,
    magentaTactical = TacticalMagenta,
    magentaDark = TacticalMagentaDark,
    magentaElectric = TacticalMagentaElectric,
    magentaContainer = TacticalMagentaContainerLight,
    orangeWarning = WarningOrange,
    orangeBright = WarningOrangeBright,
    orangeContainer = WarningOrangeContainerLight,
    greenStable = EmergencyGreenStable,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    surfaceBorder = SurfaceBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SurakshaBlueLight,
    onPrimary = Color.White,
    primaryContainer = SurakshaBlueDark,
    onPrimaryContainer = Color.White,
    secondary = SurakshaCyanAccent,
    onSecondary = Color.Black,
    tertiary = WarningOrangeBright,
    onTertiary = Color.Black,
    error = EmergencyRedCritical,
    onError = Color.White,
    errorContainer = EmergencyRedContainerDark,
    onErrorContainer = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = SurakshaBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SurakshaBlueContainerLight,
    onPrimaryContainer = SurakshaBluePrimary,
    secondary = SurakshaBlueLight,
    onSecondary = Color.White,
    tertiary = WarningOrangeDark,
    onTertiary = Color.White,
    error = EmergencyRedCritical,
    onError = Color.White,
    errorContainer = EmergencyRedContainerLight,
    onErrorContainer = EmergencyRedDark,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand contrast instead of arbitrary device tinting
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val emergencyColors = if (darkTheme) DarkEmergencyColors else LightEmergencyColors

    CompositionLocalProvider(LocalEmergencyColors provides emergencyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
