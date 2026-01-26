package com.financehub.designsystem.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.financehub.designsystem.foundation.TextVariant
import com.financehub.designsystem.foundation.toStyleConfig
import com.financehub.designsystem.tokens.DSTypography

/**
 * Design System - Text Component
 * 
 * Componente de texto unificado com variantes semânticas.
 * Usa o sistema de tokens de tipografia e cores.
 * 
 * @param text Texto a ser exibido
 * @param modifier Modifier do Compose
 * @param variant Variante semântica (BODY, TITLE, INCOME, EXPENSE, etc.)
 * @param color Override de cor (usa a cor do variant se null)
 * @param fontWeight Override de peso da fonte
 * @param textAlign Alinhamento do texto
 * @param maxLines Número máximo de linhas
 * @param overflow Comportamento de overflow
 * @param textDecoration Decoração do texto (underline, etc.)
 * 
 * @example
 * ```kotlin
 * // Texto padrão
 * DSText("Hello World")
 * 
 * // Título
 * DSText("Resumo", variant = TextVariant.HEADLINE)
 * 
 * // Valor de entrada
 * DSText("R$ 1.500,00", variant = TextVariant.INCOME)
 * 
 * // Valor de saída
 * DSText("R$ 500,00", variant = TextVariant.EXPENSE)
 * ```
 */
@Composable
fun DSText(
    text: String,
    modifier: Modifier = Modifier,
    variant: TextVariant = TextVariant.BODY,
    color: Color? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textDecoration: TextDecoration? = null
) {
    val styleConfig = variant.toStyleConfig()
    
    Text(
        text = text,
        modifier = modifier,
        style = styleConfig.style,
        color = color ?: styleConfig.color,
        fontWeight = fontWeight ?: styleConfig.fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        textDecoration = textDecoration
    )
}

/**
 * Variante para valores monetários com formatação automática
 * 
 * @param amount Valor em Double
 * @param isIncome Se é entrada (verde) ou saída (vermelho)
 * @param showSign Se deve mostrar sinal + ou -
 * @param size Tamanho do texto (SMALL, MEDIUM, LARGE)
 * 
 * @example
 * ```kotlin
 * DSMoneyText(amount = 1500.00, isIncome = true)  // R$ 1.500,00 (verde)
 * DSMoneyText(amount = 500.00, isIncome = false)  // R$ 500,00 (vermelho)
 * ```
 */
@Composable
fun DSMoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    isIncome: Boolean = amount >= 0,
    showSign: Boolean = false,
    size: MoneyTextSize = MoneyTextSize.MEDIUM
) {
    val variant = when {
        isIncome -> when (size) {
            MoneyTextSize.SMALL -> TextVariant.MONEY_SMALL
            MoneyTextSize.MEDIUM -> TextVariant.INCOME
            MoneyTextSize.LARGE -> TextVariant.MONEY_LARGE
        }
        else -> when (size) {
            MoneyTextSize.SMALL -> TextVariant.MONEY_SMALL
            MoneyTextSize.MEDIUM -> TextVariant.EXPENSE
            MoneyTextSize.LARGE -> TextVariant.MONEY_LARGE
        }
    }
    
    val styleConfig = variant.toStyleConfig()
    val formattedAmount = formatCurrency(amount, showSign)
    
    // Para MONEY_LARGE e MONEY_SMALL, precisamos aplicar a cor correta
    val finalColor = if (size != MoneyTextSize.MEDIUM) {
        if (isIncome) com.financehub.designsystem.tokens.DSColors.Income.main
        else com.financehub.designsystem.tokens.DSColors.Expense.main
    } else {
        styleConfig.color
    }
    
    Text(
        text = formattedAmount,
        modifier = modifier,
        style = styleConfig.style,
        color = finalColor,
        fontWeight = styleConfig.fontWeight
    )
}

enum class MoneyTextSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * Formata valor para moeda brasileira
 */
private fun formatCurrency(value: Double, showSign: Boolean = false): String {
    val absValue = kotlin.math.abs(value)
    val formatted = "R$ ${String.format("%,.2f", absValue).replace(",", "X").replace(".", ",").replace("X", ".")}"
    return when {
        showSign && value > 0 -> "+$formatted"
        showSign && value < 0 -> "-$formatted"
        else -> formatted
    }
}
