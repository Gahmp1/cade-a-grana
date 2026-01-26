package com.financehub.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSSpacing
import com.financehub.designsystem.tokens.DSTypography

/**
 * Design System - RadioButton Component
 * 
 * RadioButton unificado com label opcional.
 * 
 * @param selected Se o radio está selecionado
 * @param onClick Callback de clique
 * @param modifier Modifier do Compose
 * @param label Label opcional ao lado do radio
 * @param enabled Se o radio está habilitado
 * 
 * @example
 * ```kotlin
 * DSRadioButton(
 *     selected = selectedOption == "A",
 *     onClick = { selectedOption = "A" },
 *     label = "Opção A"
 * )
 * ```
 */
@Composable
fun DSRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val colors = RadioButtonDefaults.colors(
        selectedColor = DSColors.Income.main,
        unselectedColor = DSColors.Dark.border,
        disabledSelectedColor = DSColors.Income.main.copy(alpha = 0.5f),
        disabledUnselectedColor = DSColors.Dark.border.copy(alpha = 0.5f)
    )
    
    if (label != null) {
        Row(
            modifier = modifier.selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.RadioButton
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            RadioButton(
                selected = selected,
                onClick = null, // handled by Row
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
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier.size(20.dp),
            enabled = enabled,
            colors = colors
        )
    }
}

/**
 * Grupo de RadioButtons
 * 
 * @param T Tipo das opções
 * @param options Lista de opções
 * @param selectedOption Opção atualmente selecionada
 * @param onOptionSelected Callback quando uma opção é selecionada
 * @param displayText Função para converter opção em texto
 * @param horizontal Se o grupo é horizontal (default: vertical)
 * 
 * @example
 * ```kotlin
 * DSRadioGroup(
 *     options = listOf("Diário", "Semanal", "Mensal"),
 *     selectedOption = selectedFrequency,
 *     onOptionSelected = { selectedFrequency = it }
 * )
 * ```
 */
@Composable
fun <T> DSRadioGroup(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    displayText: (T) -> String = { it.toString() },
    horizontal: Boolean = false,
    enabled: Boolean = true
) {
    if (horizontal) {
        Row(
            modifier = modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.lg)
        ) {
            options.forEach { option ->
                DSRadioButton(
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                    label = displayText(option),
                    enabled = enabled
                )
            }
        }
    } else {
        Column(
            modifier = modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            options.forEach { option ->
                DSRadioButton(
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                    label = displayText(option),
                    enabled = enabled
                )
            }
        }
    }
}
