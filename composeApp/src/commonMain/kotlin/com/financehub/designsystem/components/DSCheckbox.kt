package com.financehub.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSSpacing
import com.financehub.designsystem.tokens.DSTypography

/**
 * Variantes de checkbox
 */
enum class CheckboxVariant {
    /** Checkbox padrão (verde quando checked) */
    DEFAULT,
    
    /** Checkbox de erro/exclusão (vermelho quando checked) */
    NEGATIVE
}

/**
 * Design System - Checkbox Component
 * 
 * Checkbox unificado com label opcional.
 * 
 * @param checked Estado atual do checkbox
 * @param onCheckedChange Callback quando o estado muda
 * @param modifier Modifier do Compose
 * @param variant Variante visual (DEFAULT, NEGATIVE)
 * @param label Label opcional ao lado do checkbox
 * @param enabled Se o checkbox está habilitado
 * 
 * @example
 * ```kotlin
 * // Checkbox simples
 * DSCheckbox(
 *     checked = isChecked,
 *     onCheckedChange = { isChecked = it }
 * )
 * 
 * // Checkbox com label
 * DSCheckbox(
 *     checked = rememberMe,
 *     onCheckedChange = { rememberMe = it },
 *     label = "Lembrar-me"
 * )
 * 
 * // Checkbox de exclusão
 * DSCheckbox(
 *     checked = confirmDelete,
 *     onCheckedChange = { confirmDelete = it },
 *     variant = CheckboxVariant.NEGATIVE,
 *     label = "Confirmo a exclusão"
 * )
 * ```
 */
@Composable
fun DSCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    variant: CheckboxVariant = CheckboxVariant.DEFAULT,
    label: String? = null,
    enabled: Boolean = true
) {
    val checkedColor = when (variant) {
        CheckboxVariant.DEFAULT -> DSColors.Income.main
        CheckboxVariant.NEGATIVE -> DSColors.Expense.main
    }
    
    val colors = CheckboxDefaults.colors(
        checkedColor = checkedColor,
        uncheckedColor = DSColors.Dark.border,
        checkmarkColor = DSColors.Black,
        disabledCheckedColor = checkedColor.copy(alpha = 0.5f),
        disabledUncheckedColor = DSColors.Dark.border.copy(alpha = 0.5f)
    )
    
    if (label != null) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = colors,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = DSTypography.bodyMedium,
                color = if (enabled) {
                    DSColors.Dark.textPrimary
                } else {
                    DSColors.Dark.textDisabled
                }
            )
        }
    } else {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.size(20.dp),
            enabled = enabled,
            colors = colors
        )
    }
}
