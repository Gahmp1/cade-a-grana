package com.financehub.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.foundation.CardVariant
import com.financehub.designsystem.foundation.toColors
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSElevation
import com.financehub.designsystem.tokens.DSRadius
import com.financehub.designsystem.tokens.DSSpacing

/**
 * Design System - Card Component
 * 
 * Card unificado com variantes semânticas e slots opcionais (header, content, footer).
 * Suporta clique opcional para cards interativos.
 * 
 * @param modifier Modifier do Compose
 * @param variant Variante semântica (DEFAULT, SELECTED, SUCCESS, ERROR, etc.)
 * @param onClick Callback de clique (se null, card não é clicável)
 * @param elevation Elevação do card
 * @param header Slot opcional para cabeçalho
 * @param footer Slot opcional para rodapé
 * @param content Conteúdo principal do card
 * 
 * @example
 * ```kotlin
 * // Card simples
 * DSCard {
 *     DSText("Conteúdo do card")
 * }
 * 
 * // Card com header e footer
 * DSCard(
 *     header = { DSText("Título", variant = TextVariant.TITLE) },
 *     footer = { DSButton(...) }
 * ) {
 *     DSText("Conteúdo")
 * }
 * 
 * // Card clicável
 * DSCard(
 *     variant = CardVariant.SELECTED,
 *     onClick = { }
 * ) {
 *     DSText("Clique em mim")
 * }
 * ```
 */
@Composable
fun DSCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.DEFAULT,
    onClick: (() -> Unit)? = null,
    elevation: Dp = DSElevation.sm,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = variant.toColors()
    val border = if (variant == CardVariant.OUTLINED) {
        BorderStroke(1.dp, DSColors.Dark.border)
    } else null
    
    val cardModifier = modifier.fillMaxWidth()
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = DSRadius.lg,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border
        ) {
            CardContent(header, footer, content)
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = DSRadius.lg,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border
        ) {
            CardContent(header, footer, content)
        }
    }
}

@Composable
private fun ColumnScope.CardContent(
    header: (@Composable ColumnScope.() -> Unit)?,
    footer: (@Composable ColumnScope.() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        if (header != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = DSSpacing.lg,
                        end = DSSpacing.lg,
                        top = DSSpacing.lg,
                        bottom = DSSpacing.sm
                    )
            ) {
                header()
            }
            HorizontalDivider(
                color = DSColors.Dark.borderSubtle,
                thickness = 1.dp
            )
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            content()
        }
        
        // Footer
        if (footer != null) {
            HorizontalDivider(
                color = DSColors.Dark.borderSubtle,
                thickness = 1.dp
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = DSSpacing.lg,
                        end = DSSpacing.lg,
                        top = DSSpacing.sm,
                        bottom = DSSpacing.lg
                    )
            ) {
                footer()
            }
        }
    }
}

/**
 * Card compacto sem slots de header/footer
 * Útil para itens de lista ou conteúdo simples
 * 
 * @example
 * ```kotlin
 * DSCardCompact(
 *     variant = CardVariant.SUCCESS,
 *     onClick = { }
 * ) {
 *     Row {
 *         DSText("Transação")
 *         Spacer(Modifier.weight(1f))
 *         DSMoneyText(amount = 100.0, isIncome = true)
 *     }
 * }
 * ```
 */
@Composable
fun DSCardCompact(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.DEFAULT,
    onClick: (() -> Unit)? = null,
    padding: Dp = DSSpacing.md,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = variant.toColors()
    val border = if (variant == CardVariant.OUTLINED) {
        BorderStroke(1.dp, DSColors.Dark.border)
    } else null
    
    val cardModifier = modifier.fillMaxWidth()
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = DSRadius.md,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = DSElevation.xs),
            border = border
        ) {
            Column(
                modifier = Modifier.padding(padding)
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = DSRadius.md,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = DSElevation.xs),
            border = border
        ) {
            Column(
                modifier = Modifier.padding(padding)
            ) {
                content()
            }
        }
    }
}

/**
 * Card para exibição de erros
 * 
 * @example
 * ```kotlin
 * DSErrorCard(message = "Erro ao processar arquivo")
 * ```
 */
@Composable
fun DSErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    DSCard(
        modifier = modifier,
        variant = CardVariant.ERROR
    ) {
        if (title != null) {
            DSText(
                text = title,
                variant = com.financehub.designsystem.foundation.TextVariant.TITLE,
                color = DSColors.Expense.main
            )
        }
        DSText(
            text = message,
            variant = com.financehub.designsystem.foundation.TextVariant.BODY,
            color = DSColors.Expense.light
        )
    }
}
