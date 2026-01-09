package com.financehub.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Design System - FinanceHub
 * Paleta de cores focada em finanças com preto como base
 */
object FinanceHubColors {
    // Cores Principais
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val DarkGray = Color(0xFF1A1A1A)
    val MediumGray = Color(0xFF2D2D2D)
    val LightGray = Color(0xFF4A4A4A)
    
    // Cores Semânticas - Finanças
    val Income = Color(0xFF00C853)        // Verde para entrada de dinheiro
    val IncomeLight = Color(0xFF69F0AE)   // Verde claro
    val IncomeDark = Color(0xFF00A040)    // Verde escuro
    
    val Expense = Color(0xFFFF1744)       // Vermelho para saída de dinheiro
    val ExpenseLight = Color(0xFFFF5252)  // Vermelho claro
    val ExpenseDark = Color(0xFFD50000)   // Vermelho escuro
    
    val Neutral = Color(0xFFB0BEC5)       // Cinza para neutro
    val Warning = Color(0xFFFFAB00)       // Amarelo para avisos
    val Info = Color(0xFF2196F3)          // Azul para informações
    
    // Cores de Superfície
    val Surface = DarkGray
    val SurfaceVariant = MediumGray
    val SurfaceTint = LightGray
    
    // Cores de Texto
    val OnPrimary = White
    val OnSecondary = Black
    val OnSurface = White
    val OnSurfaceVariant = Color(0xFFB0B0B0)
    val OnBackground = White
}

/**
 * Esquema de cores escuro customizado
 */
private val DarkColorScheme = darkColorScheme(
    primary = FinanceHubColors.Black,
    onPrimary = FinanceHubColors.White,
    primaryContainer = FinanceHubColors.DarkGray,
    onPrimaryContainer = FinanceHubColors.White,
    
    secondary = FinanceHubColors.White,
    onSecondary = FinanceHubColors.Black,
    secondaryContainer = FinanceHubColors.LightGray,
    onSecondaryContainer = FinanceHubColors.White,
    
    tertiary = FinanceHubColors.Income,
    onTertiary = FinanceHubColors.Black,
    tertiaryContainer = FinanceHubColors.IncomeDark,
    onTertiaryContainer = FinanceHubColors.White,
    
    error = FinanceHubColors.Expense,
    onError = FinanceHubColors.White,
    errorContainer = FinanceHubColors.ExpenseDark,
    onErrorContainer = FinanceHubColors.White,
    
    background = FinanceHubColors.Black,
    onBackground = FinanceHubColors.White,
    
    surface = FinanceHubColors.Surface,
    onSurface = FinanceHubColors.OnSurface,
    surfaceVariant = FinanceHubColors.SurfaceVariant,
    onSurfaceVariant = FinanceHubColors.OnSurfaceVariant,
    
    surfaceTint = FinanceHubColors.SurfaceTint,
    inverseSurface = FinanceHubColors.White,
    inverseOnSurface = FinanceHubColors.Black,
    
    outline = FinanceHubColors.LightGray,
    outlineVariant = FinanceHubColors.MediumGray,
)

/**
 * Tipografia customizada para o FinanceHub
 */
private val FinanceHubTypography = Typography(
    // Você pode customizar a tipografia aqui no futuro
    // Por exemplo, usando fontes específicas para números financeiros
)

/**
 * Tema principal do FinanceHub
 * Design System com foco em finanças pessoais
 */
@Composable
fun FinanceHubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = FinanceHubTypography,
        content = content
    )
}

/**
 * Extensões úteis para componentes financeiros
 */
object FinanceHubTheme {
    /**
     * Cor baseada se é entrada ou saída de dinheiro
     */
    @Composable
    fun transactionColor(isIncome: Boolean): Color {
        return if (isIncome) {
            FinanceHubColors.Income
        } else {
            FinanceHubColors.Expense
        }
    }
    
    /**
     * Cor de fundo para cards de transação
     */
    @Composable
    fun transactionCardColor(isIncome: Boolean): Color {
        return if (isIncome) {
            FinanceHubColors.IncomeDark.copy(alpha = 0.1f)
        } else {
            FinanceHubColors.ExpenseDark.copy(alpha = 0.1f)
        }
    }
    
    /**
     * Cor para indicadores de categoria
     */
    val categoryColors = listOf(
        Color(0xFF00BCD4), // Cyan
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF9800), // Orange
        Color(0xFF4CAF50), // Green
        Color(0xFFE91E63), // Pink
        Color(0xFF3F51B5), // Indigo
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF795548), // Brown
        Color(0xFF607D8B), // Blue Gray
        Color(0xFFCDDC39), // Lime
    )
    
    fun getCategoryColor(index: Int): Color {
        return categoryColors[index % categoryColors.size]
    }
}
