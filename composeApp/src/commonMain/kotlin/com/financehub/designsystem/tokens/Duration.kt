package com.financehub.designsystem.tokens

/**
 * Design System - Animation Duration Tokens
 * 
 * Durações de animação consistentes para toda a UI.
 * Valores em milissegundos.
 * 
 * @example
 * ```kotlin
 * animateColorAsState(
 *     targetValue = color,
 *     animationSpec = tween(durationMillis = DSDuration.normal)
 * )
 * ```
 */
object DSDuration {
    /** 100ms - Instantâneo (hover states, micro-feedback) */
    val instant: Int = 100
    
    /** 150ms - Rápido (toggles, checkboxes) */
    val fast: Int = 150
    
    /** 200ms - Normal (transições padrão) */
    val normal: Int = 200
    
    /** 300ms - Médio (expansão de cards, dropdowns) */
    val medium: Int = 300
    
    /** 400ms - Lento (modais, page transitions) */
    val slow: Int = 400
    
    /** 500ms - Extra lento (animações elaboradas) */
    val slower: Int = 500
}
