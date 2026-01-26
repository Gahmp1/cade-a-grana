package com.financehub.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design System - Border Radius Tokens
 * 
 * Raios de borda seguindo estética macOS/moderna.
 * Valores maiores que Material padrão para visual mais suave.
 * 
 * @example
 * ```kotlin
 * Modifier.clip(DSRadius.md)
 * Card(shape = DSRadius.lg)
 * ```
 */
object DSRadius {
    /** 0dp - Sem arredondamento */
    val none: RoundedCornerShape = RoundedCornerShape(0.dp)
    
    /** 4dp - Mínimo (badges, chips pequenos) */
    val xs: RoundedCornerShape = RoundedCornerShape(4.dp)
    
    /** 6dp - Pequeno (botões, inputs) */
    val sm: RoundedCornerShape = RoundedCornerShape(6.dp)
    
    /** 8dp - Médio (cards padrão) */
    val md: RoundedCornerShape = RoundedCornerShape(8.dp)
    
    /** 12dp - Grande (cards destacados, dialogs) */
    val lg: RoundedCornerShape = RoundedCornerShape(12.dp)
    
    /** 16dp - Extra grande (modais, sheets) */
    val xl: RoundedCornerShape = RoundedCornerShape(16.dp)
    
    /** 24dp - Jumbo (cards hero) */
    val xxl: RoundedCornerShape = RoundedCornerShape(24.dp)
    
    /** Circular completo */
    val full: RoundedCornerShape = RoundedCornerShape(percent = 50)
}

/**
 * Valores raw de radius para uso com Modifier.clip() ou outros contextos
 */
object DSRadiusValue {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 6.dp
    val md: Dp = 8.dp
    val lg: Dp = 12.dp
    val xl: Dp = 16.dp
    val xxl: Dp = 24.dp
}
