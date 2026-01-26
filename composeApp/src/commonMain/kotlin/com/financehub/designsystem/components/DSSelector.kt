package com.financehub.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSRadius
import com.financehub.designsystem.tokens.DSSpacing
import com.financehub.designsystem.tokens.DSTypography

/**
 * Design System - Selector Component
 * 
 * Dropdown selector unificado para seleção de opções.
 * Suporta opções tipadas com display customizado.
 * 
 * @param T Tipo das opções
 * @param options Lista de opções disponíveis
 * @param selectedOption Opção atualmente selecionada
 * @param onOptionSelected Callback quando uma opção é selecionada
 * @param modifier Modifier do Compose
 * @param placeholder Texto quando nenhuma opção está selecionada
 * @param displayText Função para converter opção em texto de exibição
 * @param enabled Se o selector está habilitado
 * 
 * @example
 * ```kotlin
 * // Selector simples de strings
 * DSSelector(
 *     options = listOf("Opção 1", "Opção 2", "Opção 3"),
 *     selectedOption = selected,
 *     onOptionSelected = { selected = it },
 *     placeholder = "Selecione..."
 * )
 * 
 * // Selector tipado com displayText customizado
 * DSSelector(
 *     options = categories,
 *     selectedOption = selectedCategory,
 *     onOptionSelected = { selectedCategory = it },
 *     displayText = { it.displayName }
 * )
 * ```
 */
@Composable
fun <T> DSSelector(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecione...",
    displayText: (T) -> String = { it.toString() },
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Trigger button
        Row(
            modifier = Modifier
                .clip(DSRadius.sm)
                .background(DSColors.Dark.surfaceVariant)
                .border(1.dp, DSColors.Dark.border, DSRadius.sm)
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = DSSpacing.md, vertical = DSSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.xs)
        ) {
            Text(
                text = selectedOption?.let { displayText(it) } ?: placeholder,
                style = DSTypography.bodyMedium,
                color = if (selectedOption != null) {
                    DSColors.Dark.textPrimary
                } else {
                    DSColors.Dark.textTertiary
                },
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = DSColors.Dark.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(DSColors.Dark.surfaceElevated)
                .widthIn(min = 150.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                DropdownMenuItem(
                    text = {
                        Text(
                            text = displayText(option),
                            style = DSTypography.bodyMedium,
                            color = if (isSelected) {
                                DSColors.Income.main
                            } else {
                                DSColors.Dark.textPrimary
                            }
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DSColors.Income.main,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

/**
 * Selector com opção de limpar seleção
 * 
 * @example
 * ```kotlin
 * DSNullableSelector(
 *     options = members,
 *     selectedOption = selectedMember,
 *     onOptionSelected = { selectedMember = it },
 *     nullOptionText = "Não atribuído",
 *     displayText = { it.name }
 * )
 * ```
 */
@Composable
fun <T : Any> DSNullableSelector(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecione...",
    nullOptionText: String = "Nenhum",
    displayText: (T) -> String = { it.toString() },
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(DSRadius.sm)
                .background(DSColors.Dark.surfaceVariant)
                .border(1.dp, DSColors.Dark.border, DSRadius.sm)
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = DSSpacing.md, vertical = DSSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.xs)
        ) {
            Text(
                text = selectedOption?.let { displayText(it) } ?: placeholder,
                style = DSTypography.bodyMedium,
                color = if (selectedOption != null) {
                    DSColors.Dark.textPrimary
                } else {
                    DSColors.Dark.textTertiary
                },
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = DSColors.Dark.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(DSColors.Dark.surfaceElevated)
                .widthIn(min = 150.dp)
        ) {
            // Null option
            DropdownMenuItem(
                text = {
                    Text(
                        text = nullOptionText,
                        style = DSTypography.bodyMedium,
                        color = if (selectedOption == null) {
                            DSColors.Income.main
                        } else {
                            DSColors.Dark.textSecondary
                        }
                    )
                },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                },
                trailingIcon = if (selectedOption == null) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = DSColors.Income.main,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
            
            // Regular options
            options.forEach { option ->
                val isSelected = option == selectedOption
                DropdownMenuItem(
                    text = {
                        Text(
                            text = displayText(option),
                            style = DSTypography.bodyMedium,
                            color = if (isSelected) {
                                DSColors.Income.main
                            } else {
                                DSColors.Dark.textPrimary
                            }
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DSColors.Income.main,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}
