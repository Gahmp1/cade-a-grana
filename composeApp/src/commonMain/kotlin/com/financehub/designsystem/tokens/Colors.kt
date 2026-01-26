package com.financehub.designsystem.tokens

import androidx.compose.ui.graphics.Color

/**
 * Design System - Color Tokens
 * 
 * Paleta de cores consolidada para o FinanceHub.
 * Inclui cores base, semânticas e específicas de finanças.
 * 
 * Suporta Light e Dark mode.
 */
object DSColors {
    // ============================================
    // CORES BASE (Neutras)
    // ============================================
    
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    
    // Escala de cinzas (Dark Mode)
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray850 = Color(0xFF2D2D2D)
    val Gray900 = Color(0xFF1A1A1A)
    val Gray950 = Color(0xFF0D0D0D)
    
    // ============================================
    // CORES SEMÂNTICAS - FINANÇAS
    // ============================================
    
    /** Verde - Entrada de dinheiro / Positivo */
    object Income {
        val main = Color(0xFF00C853)
        val light = Color(0xFF69F0AE)
        val dark = Color(0xFF00A040)
        val subtle = Color(0xFF00C853).copy(alpha = 0.1f)
        val onMain = Black
    }
    
    /** Vermelho - Saída de dinheiro / Negativo */
    object Expense {
        val main = Color(0xFFFF1744)
        val light = Color(0xFFFF5252)
        val dark = Color(0xFFD50000)
        val subtle = Color(0xFFFF1744).copy(alpha = 0.1f)
        val onMain = White
    }
    
    /** Amarelo - Avisos / Atenção */
    object Warning {
        val main = Color(0xFFFFAB00)
        val light = Color(0xFFFFD54F)
        val dark = Color(0xFFFF8F00)
        val subtle = Color(0xFFFFAB00).copy(alpha = 0.1f)
        val onMain = Black
    }
    
    /** Azul - Informação */
    object Info {
        val main = Color(0xFF2196F3)
        val light = Color(0xFF64B5F6)
        val dark = Color(0xFF1976D2)
        val subtle = Color(0xFF2196F3).copy(alpha = 0.1f)
        val onMain = White
    }
    
    /** Cinza - Neutro / Inativo */
    object Neutral {
        val main = Color(0xFFB0BEC5)
        val light = Color(0xFFCFD8DC)
        val dark = Color(0xFF78909C)
        val subtle = Color(0xFFB0BEC5).copy(alpha = 0.1f)
        val onMain = Black
    }
    
    // ============================================
    // CORES DE SUPERFÍCIE (Dark Mode)
    // ============================================
    
    object Dark {
        val background = Black
        val surface = Gray900
        val surfaceVariant = Gray850
        val surfaceElevated = Gray800
        val border = Gray700
        val borderSubtle = Gray850
        
        val textPrimary = White
        val textSecondary = Gray400
        val textTertiary = Gray500
        val textDisabled = Gray600
    }
    
    // ============================================
    // CORES DE SUPERFÍCIE (Light Mode)
    // ============================================
    
    object Light {
        val background = Gray100
        val surface = White
        val surfaceVariant = Gray50
        val surfaceElevated = White
        val border = Gray300
        val borderSubtle = Gray200
        
        val textPrimary = Gray900
        val textSecondary = Gray600
        val textTertiary = Gray500
        val textDisabled = Gray400
    }
    
    // ============================================
    // ATALHOS DE COR (Top-level access)
    // ============================================
    
    /** Cor primária (verde income) */
    val primary = Income.main
    
    /** Cor de alerta (amarelo) */
    val warning = Warning.main
    
    /** Cor de informação (azul) */
    val info = Info.main
    
    /** Cor de erro/despesa (vermelho) */
    val expense = Expense.main
    
    /** Cor de sucesso/receita (verde) */
    val income = Income.main
    
    // ============================================
    // CORES DE CATEGORIA (Visualização/Charts)
    // ============================================
    
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
        // Usar Math.floorMod para garantir índice positivo mesmo com valores negativos
        val safeIndex = ((index % categoryColors.size) + categoryColors.size) % categoryColors.size
        return categoryColors[safeIndex]
    }
    
    // ============================================
    // UTILITÁRIOS
    // ============================================
    
    /**
     * Parse hex string to Color
     * @param hex String no formato "#RRGGBB" ou "RRGGBB"
     */
    fun parseHex(hex: String): Color {
        val cleanHex = hex.removePrefix("#")
        return try {
            Color(("FF$cleanHex").toLong(16))
        } catch (e: Exception) {
            Gray500 // fallback
        }
    }
}
