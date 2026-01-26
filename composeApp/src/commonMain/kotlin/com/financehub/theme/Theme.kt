package com.financehub.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.financehub.designsystem.tokens.DSColors

/**
 * Esquema de cores escuro usando DSColors do Design System
 */
private val DarkColorScheme = darkColorScheme(
    primary = DSColors.Dark.background,
    onPrimary = DSColors.Dark.textPrimary,
    primaryContainer = DSColors.Dark.surface,
    onPrimaryContainer = DSColors.Dark.textPrimary,
    
    secondary = DSColors.Dark.textPrimary,
    onSecondary = DSColors.Dark.background,
    secondaryContainer = DSColors.Dark.surfaceElevated,
    onSecondaryContainer = DSColors.Dark.textPrimary,
    
    tertiary = DSColors.Income.main,
    onTertiary = DSColors.Dark.background,
    tertiaryContainer = DSColors.Income.dark,
    onTertiaryContainer = DSColors.Dark.textPrimary,
    
    error = DSColors.Expense.main,
    onError = DSColors.Dark.textPrimary,
    errorContainer = DSColors.Expense.dark,
    onErrorContainer = DSColors.Dark.textPrimary,
    
    background = DSColors.Dark.background,
    onBackground = DSColors.Dark.textPrimary,
    
    surface = DSColors.Dark.surface,
    onSurface = DSColors.Dark.textPrimary,
    surfaceVariant = DSColors.Dark.surfaceElevated,
    onSurfaceVariant = DSColors.Dark.textSecondary,
    
    surfaceTint = DSColors.Dark.surfaceElevated,
    inverseSurface = DSColors.Dark.textPrimary,
    inverseOnSurface = DSColors.Dark.background,
    
    outline = DSColors.Dark.border,
    outlineVariant = DSColors.Dark.borderSubtle,
)

/**
 * Tema principal do Cadê a Grana?
 * Integrado com o Design System (DSColors, DSTypography)
 */
@Composable
fun CadeAGranaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
