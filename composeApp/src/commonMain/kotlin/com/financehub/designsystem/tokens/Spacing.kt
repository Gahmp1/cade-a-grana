package com.financehub.designsystem.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design System - Spacing Tokens
 * 
 * Sistema de espaçamento baseado em grid de 4pt (Tailwind-inspired).
 * Use esses tokens em vez de valores hardcoded para consistência.
 * 
 * @example
 * ```kotlin
 * Modifier.padding(DSSpacing.md)
 * Arrangement.spacedBy(DSSpacing.sm)
 * ```
 */
object DSSpacing {
    /** 0dp - Sem espaçamento */
    val none: Dp = 0.dp
    
    /** 2dp - Micro espaçamento (entre ícone e texto inline) */
    val xxs: Dp = 2.dp
    
    /** 4dp - Extra pequeno (gaps mínimos) */
    val xs: Dp = 4.dp
    
    /** 8dp - Pequeno (espaçamento entre elementos relacionados) */
    val sm: Dp = 8.dp
    
    /** 12dp - Médio-pequeno (padding interno de badges) */
    val md: Dp = 12.dp
    
    /** 16dp - Médio (padding padrão de cards e seções) */
    val lg: Dp = 16.dp
    
    /** 20dp - Médio-grande (separação entre cards) */
    val xl: Dp = 20.dp
    
    /** 24dp - Grande (separação entre seções) */
    val xxl: Dp = 24.dp
    
    /** 32dp - Extra grande (margens de página) */
    val xxxl: Dp = 32.dp
    
    /** 48dp - Jumbo (separação de áreas principais) */
    val jumbo: Dp = 48.dp
    
    /** 64dp - Super (altura mínima de toolbar) */
    val super_: Dp = 64.dp
}

/**
 * Espaçamentos específicos para componentes
 */
object DSComponentSpacing {
    /** Padding interno de botões */
    val buttonPaddingHorizontal: Dp = DSSpacing.lg
    val buttonPaddingVertical: Dp = DSSpacing.sm
    
    /** Padding interno de cards */
    val cardPadding: Dp = DSSpacing.lg
    val cardPaddingCompact: Dp = DSSpacing.md
    
    /** Padding interno de badges */
    val badgePaddingHorizontal: Dp = DSSpacing.sm
    val badgePaddingVertical: Dp = DSSpacing.xs
    
    /** Padding interno de text fields */
    val textFieldPadding: Dp = DSSpacing.md
    
    /** Gap entre itens de lista */
    val listItemGap: Dp = DSSpacing.sm
    
    /** Gap entre seções */
    val sectionGap: Dp = DSSpacing.xl
    
    /** Largura da sidebar */
    val sidebarWidth: Dp = 240.dp
    
    /** Altura do toolbar */
    val toolbarHeight: Dp = 52.dp
}
