package com.mygoal_healthtracker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = LightGreen,
    onPrimary = Color.Black,
    primaryContainer = LightGreenDeep,
    onPrimaryContainer = Color(0xFFE8FFE0),
    secondary = SkyBlue,
    onSecondary = Color.Black,
    secondaryContainer = SkyBlueDeep,
    onSecondaryContainer = Color(0xFFE0F4FF),
    tertiary = LightPurple,
    onTertiary = Color.Black,
    tertiaryContainer = LightPurpleDeep,
    onTertiaryContainer = Color(0xFFEFE5FF),
    background = AppBlack,
    onBackground = Color.White,
    surface = AppBlack,
    onSurface = Color.White,
    surfaceVariant = Panel,
    onSurfaceVariant = Color(0xFFCBD5E0),
    surfaceContainer = Panel,
    surfaceContainerHigh = PanelHigh,
    surfaceContainerHighest = PanelHighest,
    outline = Color(0xFF2A323A),
    outlineVariant = Color(0xFF1A2128),
)

@Composable
fun MyGoalHealthTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content,
    )
}
