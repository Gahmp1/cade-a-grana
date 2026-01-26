package com.financehub.designsystem.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design System - Elevation Tokens
 * 
 * Elevações sutis seguindo estética macOS.
 * Sombras mais difusas e menos pronunciadas que Material padrão.
 * 
 * @example
 * ```kotlin
 * Card(elevation = CardDefaults.cardElevation(defaultElevation = DSElevation.sm))
 * ```
 */
object DSElevation {
    /** 0dp - Sem elevação (elementos inline) */
    val none: Dp = 0.dp
    
    /** 1dp - Mínima (cards flat) */
    val xs: Dp = 1.dp
    
    /** 2dp - Pequena (cards padrão) */
    val sm: Dp = 2.dp
    
    /** 4dp - Média (cards elevados, dropdowns) */
    val md: Dp = 4.dp
    
    /** 8dp - Grande (dialogs, modais) */
    val lg: Dp = 8.dp
    
    /** 12dp - Extra grande (popovers flutuantes) */
    val xl: Dp = 12.dp
    
    /** 16dp - Máxima (drag & drop) */
    val xxl: Dp = 16.dp
}
