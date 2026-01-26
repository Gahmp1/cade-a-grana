package com.financehub.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.foundation.BadgeVariant
import com.financehub.designsystem.foundation.TextVariant
import com.financehub.designsystem.foundation.toStyleConfig
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSRadius
import com.financehub.designsystem.tokens.DSSpacing
import com.financehub.designsystem.tokens.DSTypography

/**
 * Design System - Badge Component
 * 
 * Badge/chip para exibição de labels, categorias, status, etc.
 * Suporta variantes semânticas e cores customizadas.
 * 
 * @param text Texto do badge
 * @param modifier Modifier do Compose
 * @param variant Variante semântica (DEFAULT, CATEGORY, OWNER, SUCCESS, ERROR, etc.)
 * @param backgroundColor Override de cor de fundo (para cores dinâmicas como categoria)
 * @param contentColor Override de cor do conteúdo
 * @param leadingIcon Ícone opcional à esquerda
 * 
 * @example
 * ```kotlin
 * // Badge padrão
 * DSBadge(text = "Novo")
 * 
 * // Badge de categoria
 * DSBadge(
 *     text = "Alimentação",
 *     variant = BadgeVariant.CATEGORY
 * )
 * 
 * // Badge de sucesso
 * DSBadge(
 *     text = "Aprovado",
 *     variant = BadgeVariant.SUCCESS
 * )
 * 
 * // Badge com cor customizada (ex: cor do membro)
 * DSBadge(
 *     text = "João",
 *     backgroundColor = memberColor.copy(alpha = 0.2f),
 *     contentColor = memberColor
 * )
 * ```
 */
@Composable
fun DSBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.DEFAULT,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    leadingIcon: ImageVector? = null
) {
    val styleConfig = variant.toStyleConfig()
    val bgColor = backgroundColor ?: styleConfig.backgroundColor
    val fgColor = contentColor ?: styleConfig.contentColor
    
    Box(
        modifier = modifier
            .clip(DSRadius.xs)
            .background(bgColor)
            .padding(
                horizontal = DSSpacing.sm,
                vertical = DSSpacing.xs
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.xs)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = fgColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                style = DSTypography.labelMedium,
                color = fgColor
            )
        }
    }
}

/**
 * Badge para exibição de categoria de transação
 * 
 * @param categoryName Nome da categoria
 * @param categoryIndex Índice para cor (opcional, usa cor da paleta)
 * 
 * @example
 * ```kotlin
 * DSCategoryBadge(
 *     categoryName = "Alimentação"
 * )
 * ```
 */
@Composable
fun DSCategoryBadge(
    categoryName: String,
    modifier: Modifier = Modifier,
    categoryIndex: Int? = null
) {
    val categoryColor = if (categoryIndex != null) {
        DSColors.getCategoryColor(categoryIndex)
    } else {
        // Gera índice baseado no hash do nome
        DSColors.getCategoryColor(categoryName.hashCode())
    }
    
    DSBadge(
        text = categoryName,
        modifier = modifier,
        variant = BadgeVariant.CATEGORY,
        backgroundColor = categoryColor.copy(alpha = 0.15f),
        contentColor = categoryColor
    )
}

/**
 * Badge para exibição de owner/membro
 * 
 * @param ownerName Nome do membro
 * @param ownerColor Cor do membro (hex string ou Color)
 * 
 * @example
 * ```kotlin
 * DSOwnerBadge(
 *     ownerName = "João",
 *     ownerColor = Color.Blue
 * )
 * ```
 */
@Composable
fun DSOwnerBadge(
    ownerName: String,
    ownerColor: Color,
    modifier: Modifier = Modifier
) {
    DSBadge(
        text = ownerName,
        modifier = modifier,
        variant = BadgeVariant.OWNER,
        backgroundColor = ownerColor.copy(alpha = 0.2f),
        contentColor = ownerColor
    )
}

/**
 * Badge para status com variantes pré-definidas
 * 
 * @example
 * ```kotlin
 * DSStatusBadge(text = "Pendente", variant = BadgeVariant.WARNING)
 * DSStatusBadge(text = "Concluído", variant = BadgeVariant.SUCCESS)
 * DSStatusBadge(text = "Erro", variant = BadgeVariant.ERROR)
 * ```
 */
@Composable
fun DSStatusBadge(
    text: String,
    variant: BadgeVariant,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    DSBadge(
        text = text,
        modifier = modifier,
        variant = variant,
        leadingIcon = leadingIcon
    )
}
