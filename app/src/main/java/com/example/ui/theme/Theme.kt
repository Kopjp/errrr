package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = OlivePrimaryDark,
    onPrimary = OliveOnPrimaryDark,
    primaryContainer = OlivePrimaryContainerDark,
    onPrimaryContainer = OliveOnPrimaryContainerDark,
    secondary = OliveSecondaryDark,
    onSecondary = OliveOnSecondaryDark,
    secondaryContainer = OliveSecondaryContainerDark,
    onSecondaryContainer = OliveOnSecondaryContainerDark,
    tertiary = OliveTertiaryDark,
    onTertiary = OliveOnTertiaryDark,
    tertiaryContainer = OliveTertiaryContainerDark,
    onTertiaryContainer = OliveOnTertiaryContainerDark,
    background = OliveBackgroundDark,
    onBackground = OliveOnBackgroundDark,
    surface = OliveSurfaceDark,
    onSurface = OliveOnSurfaceDark,
    surfaceVariant = OliveSurfaceVariantDark,
    onSurfaceVariant = OliveOnSurfaceVariantDark,
    outline = OliveOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OlivePrimary,
    onPrimary = OliveOnPrimary,
    primaryContainer = OlivePrimaryContainer,
    onPrimaryContainer = OliveOnPrimaryContainer,
    secondary = OliveSecondary,
    onSecondary = OliveOnSecondary,
    secondaryContainer = OliveSecondaryContainer,
    onSecondaryContainer = OliveOnSecondaryContainer,
    tertiary = OliveTertiary,
    onTertiary = OliveOnTertiary,
    tertiaryContainer = OliveTertiaryContainer,
    onTertiaryContainer = OliveOnTertiaryContainer,
    background = OliveBackgroundLight,
    onBackground = OliveOnBackgroundLight,
    surface = OliveSurfaceLight,
    onSurface = OliveOnSurfaceLight,
    surfaceVariant = OliveSurfaceVariantLight,
    onSurfaceVariant = OliveOnSurfaceVariantLight,
    outline = OliveOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Default to our bespoke olive branding
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
