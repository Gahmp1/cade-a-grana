package com.financehub.designsystem.foundation

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSTypography

/**
 * Design System - Variants
 * 
 * Enums semânticos que definem variações visuais dos componentes.
 * Cada variant tem uma extension function que resolve para os estilos do Material 3.
 * 
 * @example
 * ```kotlin
 * DSButton(
 *     variant = ButtonVariant.POSITIVE,
 *     onClick = { ... }
 * ) { Text("Confirmar") }
 * ```
 */

// ============================================
// BUTTON VARIANTS
// ============================================

/**
 * Variantes semânticas para botões
 */
enum class ButtonVariant {
    /** Ação principal - fundo sólido escuro */
    PRIMARY,
    
    /** Ação secundária - apenas borda */
    SECONDARY,
    
    /** Ação positiva - confirmar, salvar (verde) */
    POSITIVE,
    
    /** Ação negativa - excluir, cancelar (vermelho) */
    NEGATIVE,
    
    /** Ação sutil - mínimo peso visual */
    GHOST,
    
    /** Ação de atenção - avisos (amarelo) */
    WARNING
}

@Composable
fun ButtonVariant.toColors(): ButtonColors = when (this) {
    ButtonVariant.PRIMARY -> ButtonDefaults.buttonColors(
        containerColor = DSColors.Dark.surface,
        contentColor = DSColors.Dark.textPrimary,
        disabledContainerColor = DSColors.Gray800,
        disabledContentColor = DSColors.Gray600
    )
    ButtonVariant.SECONDARY -> ButtonDefaults.outlinedButtonColors(
        contentColor = DSColors.Dark.textPrimary,
        disabledContentColor = DSColors.Gray600
    )
    ButtonVariant.POSITIVE -> ButtonDefaults.buttonColors(
        containerColor = DSColors.Income.main,
        contentColor = DSColors.Income.onMain,
        disabledContainerColor = DSColors.Income.dark.copy(alpha = 0.5f),
        disabledContentColor = DSColors.Gray600
    )
    ButtonVariant.NEGATIVE -> ButtonDefaults.buttonColors(
        containerColor = DSColors.Expense.main,
        contentColor = DSColors.Expense.onMain,
        disabledContainerColor = DSColors.Expense.dark.copy(alpha = 0.5f),
        disabledContentColor = DSColors.Gray600
    )
    ButtonVariant.GHOST -> ButtonDefaults.textButtonColors(
        contentColor = DSColors.Dark.textPrimary,
        disabledContentColor = DSColors.Gray600
    )
    ButtonVariant.WARNING -> ButtonDefaults.buttonColors(
        containerColor = DSColors.Warning.main,
        contentColor = DSColors.Warning.onMain,
        disabledContainerColor = DSColors.Warning.dark.copy(alpha = 0.5f),
        disabledContentColor = DSColors.Gray600
    )
}

// ============================================
// TEXT VARIANTS
// ============================================

/**
 * Variantes semânticas para texto
 */
enum class TextVariant {
    /** Título de página principal */
    DISPLAY,
    
    /** Título de seção */
    HEADLINE,
    
    /** Título de card/componente */
    TITLE,
    
    /** Texto de conteúdo padrão */
    BODY,
    
    /** Texto secundário/caption */
    CAPTION,
    
    /** Rótulo pequeno */
    LABEL,
    
    /** Valor de entrada de dinheiro (verde) */
    INCOME,
    
    /** Valor de entrada de dinheiro grande (verde, headline) */
    INCOME_LARGE,
    
    /** Valor de saída de dinheiro (vermelho) */
    EXPENSE,
    
    /** Valor de saída de dinheiro grande (vermelho, headline) */
    EXPENSE_LARGE,
    
    /** Valor neutro/balanceado */
    NEUTRAL,
    
    /** Valor monetário grande (destaque) */
    MONEY_LARGE,
    
    /** Valor monetário padrão */
    MONEY,
    
    /** Valor monetário pequeno */
    MONEY_SMALL
}

data class TextStyleConfig(
    val style: TextStyle,
    val color: Color,
    val fontWeight: FontWeight? = null
)

@Composable
fun TextVariant.toStyleConfig(): TextStyleConfig = when (this) {
    TextVariant.DISPLAY -> TextStyleConfig(
        style = DSTypography.displaySmall,
        color = DSColors.Dark.textPrimary
    )
    TextVariant.HEADLINE -> TextStyleConfig(
        style = DSTypography.headlineMedium,
        color = DSColors.Dark.textPrimary
    )
    TextVariant.TITLE -> TextStyleConfig(
        style = DSTypography.titleLarge,
        color = DSColors.Dark.textPrimary
    )
    TextVariant.BODY -> TextStyleConfig(
        style = DSTypography.bodyMedium,
        color = DSColors.Dark.textPrimary
    )
    TextVariant.CAPTION -> TextStyleConfig(
        style = DSTypography.bodySmall,
        color = DSColors.Dark.textSecondary
    )
    TextVariant.LABEL -> TextStyleConfig(
        style = DSTypography.labelMedium,
        color = DSColors.Dark.textTertiary
    )
    TextVariant.INCOME -> TextStyleConfig(
        style = DSTypography.money,
        color = DSColors.Income.main,
        fontWeight = FontWeight.Bold
    )
    TextVariant.INCOME_LARGE -> TextStyleConfig(
        style = DSTypography.headlineMedium,
        color = DSColors.Income.main,
        fontWeight = FontWeight.Bold
    )
    TextVariant.EXPENSE -> TextStyleConfig(
        style = DSTypography.money,
        color = DSColors.Expense.main,
        fontWeight = FontWeight.Bold
    )
    TextVariant.EXPENSE_LARGE -> TextStyleConfig(
        style = DSTypography.headlineMedium,
        color = DSColors.Expense.main,
        fontWeight = FontWeight.Bold
    )
    TextVariant.NEUTRAL -> TextStyleConfig(
        style = DSTypography.money,
        color = DSColors.Neutral.main,
        fontWeight = FontWeight.Bold
    )
    TextVariant.MONEY_LARGE -> TextStyleConfig(
        style = DSTypography.moneyLarge,
        color = DSColors.Dark.textPrimary,
        fontWeight = FontWeight.Bold
    )
    TextVariant.MONEY -> TextStyleConfig(
        style = DSTypography.money,
        color = DSColors.Dark.textPrimary,
        fontWeight = FontWeight.Bold
    )
    TextVariant.MONEY_SMALL -> TextStyleConfig(
        style = DSTypography.moneySmall,
        color = DSColors.Dark.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
}

// ============================================
// CARD VARIANTS
// ============================================

/**
 * Variantes semânticas para cards
 */
enum class CardVariant {
    /** Card padrão - superfície normal */
    DEFAULT,
    
    /** Card selecionado/ativo */
    SELECTED,
    
    /** Card elevado - destaque */
    ELEVATED,
    
    /** Card de sucesso/positivo (verde sutil) */
    SUCCESS,
    
    /** Card de erro/negativo (vermelho sutil) */
    ERROR,
    
    /** Card de aviso (amarelo sutil) */
    WARNING,
    
    /** Card de informação (azul sutil) */
    INFO,
    
    /** Card desabilitado/muted */
    MUTED,
    
    /** Card outlined - apenas borda */
    OUTLINED
}

@Composable
fun CardVariant.toColors(): CardColors = when (this) {
    CardVariant.DEFAULT -> CardDefaults.cardColors(
        containerColor = DSColors.Dark.surface,
        contentColor = DSColors.Dark.textPrimary
    )
    CardVariant.SELECTED -> CardDefaults.cardColors(
        containerColor = DSColors.Dark.surfaceElevated,
        contentColor = DSColors.Dark.textPrimary
    )
    CardVariant.ELEVATED -> CardDefaults.elevatedCardColors(
        containerColor = DSColors.Dark.surfaceVariant,
        contentColor = DSColors.Dark.textPrimary
    )
    CardVariant.SUCCESS -> CardDefaults.cardColors(
        containerColor = DSColors.Income.subtle,
        contentColor = DSColors.Dark.textPrimary
    )
    CardVariant.ERROR -> CardDefaults.cardColors(
        containerColor = DSColors.Expense.subtle,
        contentColor = DSColors.Dark.textPrimary
    )
    CardVariant.WARNING -> CardDefaults.cardColors(
        containerColor = DSColors.Warning.subtle,
        contentColor = DSColors.Dark.textPrimary
    )
    CardVariant.INFO -> CardDefaults.cardColors(
        containerColor = DSColors.Info.subtle,
        contentColor = DSColors.Dark.textPrimary
    )
    CardVariant.MUTED -> CardDefaults.cardColors(
        containerColor = DSColors.Dark.surface.copy(alpha = 0.5f),
        contentColor = DSColors.Dark.textTertiary
    )
    CardVariant.OUTLINED -> CardDefaults.outlinedCardColors(
        containerColor = Color.Transparent,
        contentColor = DSColors.Dark.textPrimary
    )
}

// ============================================
// TEXT FIELD VARIANTS
// ============================================

/**
 * Variantes semânticas para campos de texto
 */
enum class TextFieldVariant {
    /** Campo padrão */
    DEFAULT,
    
    /** Campo de busca (com ícone de lupa) */
    SEARCH,
    
    /** Campo com erro */
    ERROR,
    
    /** Campo desabilitado */
    DISABLED
}

@Composable
fun TextFieldVariant.toColors(): TextFieldColors = when (this) {
    TextFieldVariant.DEFAULT -> TextFieldDefaults.colors(
        focusedContainerColor = DSColors.Dark.surfaceVariant,
        unfocusedContainerColor = DSColors.Dark.surface,
        focusedIndicatorColor = DSColors.Income.main,
        unfocusedIndicatorColor = DSColors.Dark.border,
        focusedTextColor = DSColors.Dark.textPrimary,
        unfocusedTextColor = DSColors.Dark.textPrimary,
        cursorColor = DSColors.Income.main
    )
    TextFieldVariant.SEARCH -> TextFieldDefaults.colors(
        focusedContainerColor = DSColors.Dark.surfaceVariant,
        unfocusedContainerColor = DSColors.Dark.surfaceVariant,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = DSColors.Dark.textPrimary,
        unfocusedTextColor = DSColors.Dark.textPrimary,
        cursorColor = DSColors.Income.main
    )
    TextFieldVariant.ERROR -> TextFieldDefaults.colors(
        focusedContainerColor = DSColors.Expense.subtle,
        unfocusedContainerColor = DSColors.Expense.subtle,
        focusedIndicatorColor = DSColors.Expense.main,
        unfocusedIndicatorColor = DSColors.Expense.main,
        focusedTextColor = DSColors.Dark.textPrimary,
        unfocusedTextColor = DSColors.Dark.textPrimary,
        cursorColor = DSColors.Expense.main
    )
    TextFieldVariant.DISABLED -> TextFieldDefaults.colors(
        disabledContainerColor = DSColors.Dark.surface.copy(alpha = 0.5f),
        disabledIndicatorColor = Color.Transparent,
        disabledTextColor = DSColors.Dark.textDisabled
    )
}

// ============================================
// BADGE VARIANTS
// ============================================

/**
 * Variantes semânticas para badges
 */
enum class BadgeVariant {
    /** Badge padrão - cinza neutro */
    DEFAULT,
    
    /** Badge de categoria - cor dinâmica */
    CATEGORY,
    
    /** Badge de owner/membro - cor do membro */
    OWNER,
    
    /** Badge de sucesso (verde) */
    SUCCESS,
    
    /** Badge de erro (vermelho) */
    ERROR,
    
    /** Badge de info (azul) */
    INFO,
    
    /** Badge de warning (amarelo) */
    WARNING
}

data class BadgeStyleConfig(
    val backgroundColor: Color,
    val contentColor: Color
)

@Composable
fun BadgeVariant.toStyleConfig(): BadgeStyleConfig = when (this) {
    BadgeVariant.DEFAULT -> BadgeStyleConfig(
        backgroundColor = DSColors.Dark.surfaceVariant,
        contentColor = DSColors.Dark.textSecondary
    )
    BadgeVariant.CATEGORY -> BadgeStyleConfig(
        backgroundColor = DSColors.Dark.surfaceVariant,
        contentColor = DSColors.Dark.textPrimary
    )
    BadgeVariant.OWNER -> BadgeStyleConfig(
        backgroundColor = DSColors.Dark.surfaceVariant,
        contentColor = DSColors.Dark.textPrimary
    )
    BadgeVariant.SUCCESS -> BadgeStyleConfig(
        backgroundColor = DSColors.Income.subtle,
        contentColor = DSColors.Income.main
    )
    BadgeVariant.ERROR -> BadgeStyleConfig(
        backgroundColor = DSColors.Expense.subtle,
        contentColor = DSColors.Expense.main
    )
    BadgeVariant.INFO -> BadgeStyleConfig(
        backgroundColor = DSColors.Info.subtle,
        contentColor = DSColors.Info.main
    )
    BadgeVariant.WARNING -> BadgeStyleConfig(
        backgroundColor = DSColors.Warning.subtle,
        contentColor = DSColors.Warning.main
    )
}

// ============================================
// SIZE VARIANTS (Shared)
// ============================================

/**
 * Tamanhos padrão para componentes
 */
enum class ComponentSize {
    /** Extra pequeno */
    XS,
    
    /** Pequeno */
    SM,
    
    /** Médio (padrão) */
    MD,
    
    /** Grande */
    LG,
    
    /** Extra grande */
    XL
}
