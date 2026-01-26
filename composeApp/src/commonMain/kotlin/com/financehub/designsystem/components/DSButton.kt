package com.financehub.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.foundation.ButtonVariant
import com.financehub.designsystem.foundation.ComponentSize
import com.financehub.designsystem.foundation.toColors
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSRadius
import com.financehub.designsystem.tokens.DSSpacing
import com.financehub.designsystem.tokens.DSTypography

/**
 * Design System - Button Component
 * 
 * Botão unificado com variantes semânticas e tamanhos.
 * Suporta ícones leading/trailing.
 * 
 * @param onClick Callback de clique
 * @param modifier Modifier do Compose
 * @param variant Variante semântica (PRIMARY, SECONDARY, POSITIVE, NEGATIVE, GHOST)
 * @param size Tamanho do botão (SM, MD, LG)
 * @param enabled Se o botão está habilitado
 * @param leadingIcon Ícone à esquerda do texto
 * @param trailingIcon Ícone à direita do texto
 * @param content Conteúdo do botão (geralmente Text)
 * 
 * @example
 * ```kotlin
 * // Botão primário
 * DSButton(onClick = { }) {
 *     Text("Selecionar Arquivos")
 * }
 * 
 * // Botão positivo com ícone
 * DSButton(
 *     onClick = { },
 *     variant = ButtonVariant.POSITIVE,
 *     leadingIcon = Icons.Default.Check
 * ) {
 *     Text("Confirmar")
 * }
 * 
 * // Botão negativo
 * DSButton(
 *     onClick = { },
 *     variant = ButtonVariant.NEGATIVE
 * ) {
 *     Text("Excluir")
 * }
 * ```
 */
@Composable
fun DSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    size: ComponentSize = ComponentSize.MD,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    val colors = variant.toColors()
    val (height, padding, iconSize) = when (size) {
        ComponentSize.XS -> Triple(28.dp, PaddingValues(horizontal = 8.dp), 14.dp)
        ComponentSize.SM -> Triple(32.dp, PaddingValues(horizontal = 12.dp), 16.dp)
        ComponentSize.MD -> Triple(40.dp, PaddingValues(horizontal = 16.dp), 18.dp)
        ComponentSize.LG -> Triple(48.dp, PaddingValues(horizontal = 20.dp), 20.dp)
        ComponentSize.XL -> Triple(56.dp, PaddingValues(horizontal = 24.dp), 24.dp)
    }
    
    when (variant) {
        ButtonVariant.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(height),
                enabled = enabled,
                colors = colors,
                shape = DSRadius.sm,
                border = BorderStroke(1.dp, if (enabled) DSColors.Dark.border else DSColors.Gray700),
                contentPadding = padding
            ) {
                ButtonContent(leadingIcon, trailingIcon, iconSize, content)
            }
        }
        ButtonVariant.GHOST -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.height(height),
                enabled = enabled,
                colors = colors,
                shape = DSRadius.sm,
                contentPadding = padding
            ) {
                ButtonContent(leadingIcon, trailingIcon, iconSize, content)
            }
        }
        else -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(height),
                enabled = enabled,
                colors = colors,
                shape = DSRadius.sm,
                contentPadding = padding
            ) {
                ButtonContent(leadingIcon, trailingIcon, iconSize, content)
            }
        }
    }
}

@Composable
private fun RowScope.ButtonContent(
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    iconSize: androidx.compose.ui.unit.Dp,
    content: @Composable RowScope.() -> Unit
) {
    if (leadingIcon != null) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
        Spacer(Modifier.width(DSSpacing.xs))
    }
    content()
    if (trailingIcon != null) {
        Spacer(Modifier.width(DSSpacing.xs))
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Variante simplificada do DSButton apenas com texto
 * 
 * @example
 * ```kotlin
 * DSSimpleTextButton(
 *     text = "Confirmar",
 *     onClick = { },
 *     variant = ButtonVariant.POSITIVE
 * )
 * ```
 */
@Composable
fun DSSimpleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    size: ComponentSize = ComponentSize.MD,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    val textStyle = when (size) {
        ComponentSize.XS -> DSTypography.labelSmall
        ComponentSize.SM -> DSTypography.labelMedium
        ComponentSize.MD -> DSTypography.labelLarge
        ComponentSize.LG -> DSTypography.titleSmall
        ComponentSize.XL -> DSTypography.titleMedium
    }
    
    DSButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    ) {
        Text(text = text, style = textStyle)
    }
}

/**
 * Botão de texto estilo TextButton (transparente)
 * 
 * @example
 * ```kotlin
 * DSTextButton(onClick = { }) {
 *     Text("Cancelar")
 * }
 * ```
 */
@Composable
fun DSTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = DSColors.Dark.textPrimary,
            disabledContentColor = DSColors.Gray600
        )
    ) {
        content()
    }
}

/**
 * Botão de ícone (circular ou quadrado)
 * 
 * @example
 * ```kotlin
 * DSIconButton(
 *     icon = Icons.Default.Close,
 *     onClick = { },
 *     contentDescription = "Fechar"
 * )
 * ```
 */
@Composable
fun DSIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.GHOST,
    size: ComponentSize = ComponentSize.MD,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val colors = variant.toColors()
    val buttonSize = when (size) {
        ComponentSize.XS -> 24.dp
        ComponentSize.SM -> 32.dp
        ComponentSize.MD -> 40.dp
        ComponentSize.LG -> 48.dp
        ComponentSize.XL -> 56.dp
    }
    val iconSize = when (size) {
        ComponentSize.XS -> 14.dp
        ComponentSize.SM -> 16.dp
        ComponentSize.MD -> 20.dp
        ComponentSize.LG -> 24.dp
        ComponentSize.XL -> 28.dp
    }
    
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        enabled = enabled,
        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
            contentColor = when (variant) {
                ButtonVariant.POSITIVE -> DSColors.Income.main
                ButtonVariant.NEGATIVE -> DSColors.Expense.main
                ButtonVariant.WARNING -> DSColors.Warning.main
                else -> DSColors.Dark.textPrimary
            },
            disabledContentColor = DSColors.Gray600
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )
    }
}
