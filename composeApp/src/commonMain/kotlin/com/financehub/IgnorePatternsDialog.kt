package com.financehub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*
import com.financehub.presentation.FinanceEvent
import com.financehub.presentation.FinanceState

@Composable
fun IgnorePatternsDialog(
    state: FinanceState,
    onEvent: (FinanceEvent) -> Unit
) {
    var newPattern by remember { mutableStateOf("") }
    var showAdvancedSection by remember { mutableStateOf(false) }
    
    // Campos para regra avançada
    var advPattern by remember { mutableStateOf("") }
    var advMinAmount by remember { mutableStateOf("3000") }
    var advRequiresInteger by remember { mutableStateOf(true) }
    var advDescription by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { onEvent(FinanceEvent.HideIgnorePatternsDialog) },
        icon = {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        },
        title = {
            Text("Padrões para Ignorar")
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DSText(
                    text = "Transações contendo essas palavras serão automaticamente marcadas como ignoradas.",
                    variant = TextVariant.CAPTION
                )
                
                // Seção: Padrões Simples
                DSText(
                    text = "Padrões Simples",
                    variant = TextVariant.TITLE
                )
                
                // Campo para adicionar novo padrão
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        modifier = Modifier.weight(1f),
                        placeholder = "Ex: aporte, resgate..."
                    )
                    
                    DSIconButton(
                        onClick = {
                            if (newPattern.isNotBlank()) {
                                onEvent(FinanceEvent.AddIgnorePattern(newPattern))
                                newPattern = ""
                            }
                        },
                        enabled = newPattern.isNotBlank(),
                        icon = Icons.Default.Add,
                        contentDescription = "Adicionar"
                    )
                }
                
                // Lista de padrões simples
                if (state.ignorePatterns.patterns.isEmpty()) {
                    DSText(
                        text = "Nenhum padrão configurado",
                        variant = TextVariant.CAPTION,
                        modifier = Modifier.padding(vertical = DSSpacing.sm)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.ignorePatterns.patterns) { pattern ->
                            PatternItem(
                                pattern = pattern,
                                onRemove = { onEvent(FinanceEvent.RemoveIgnorePattern(pattern)) }
                            )
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Seção: Regras Avançadas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSText(
                        text = "Regras Avançadas (${state.ignorePatterns.advancedRules.size})",
                        variant = TextVariant.TITLE
                    )
                    DSIconButton(
                        onClick = { showAdvancedSection = !showAdvancedSection },
                        icon = if (showAdvancedSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                
                if (showAdvancedSection) {
                    DSText(
                        text = "Combine padrão de texto + condições de valor. Transações marcadas aparecem para revisão.",
                        variant = TextVariant.CAPTION
                    )
                    
                    // Formulário de regra avançada
                    DSCard(variant = CardVariant.DEFAULT) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
                        ) {
                            DSTextField(
                                value = advPattern,
                                onValueChange = { advPattern = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = "Padrão de texto",
                                placeholder = "Ex: pix recebido"
                            )
                            
                            DSTextField(
                                value = advMinAmount,
                                onValueChange = { advMinAmount = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = "Valor mínimo (R$)",
                                placeholder = "3000"
                            )
                            
                            DSCheckbox(
                                checked = advRequiresInteger,
                                onCheckedChange = { advRequiresInteger = it },
                                label = "Apenas valores redondos (múltiplos de 1000)"
                            )
                            
                            DSTextField(
                                value = advDescription,
                                onValueChange = { advDescription = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = "Descrição (opcional)",
                                placeholder = "Ex: Transferências grandes"
                            )
                            
                            DSButton(
                                onClick = {
                                    val amount = advMinAmount.toDoubleOrNull() ?: 0.0
                                    if (advPattern.isNotBlank() && amount > 0) {
                                        onEvent(FinanceEvent.AddAdvancedIgnoreRule(
                                            pattern = advPattern,
                                            minAmount = amount,
                                            requiresIntegerValue = advRequiresInteger,
                                            description = advDescription
                                        ))
                                        advPattern = ""
                                        advMinAmount = "3000"
                                        advRequiresInteger = true
                                        advDescription = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = advPattern.isNotBlank() && advMinAmount.toDoubleOrNull() != null,
                                variant = ButtonVariant.PRIMARY,
                                leadingIcon = Icons.Default.Add
                            ) {
                                Text("Adicionar Regra Avançada")
                            }
                        }
                    }
                    
                    // Lista de regras avançadas
                    if (state.ignorePatterns.advancedRules.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.ignorePatterns.advancedRules) { rule ->
                                AdvancedRuleItem(
                                    rule = rule,
                                    onRemove = { onEvent(FinanceEvent.RemoveAdvancedIgnoreRule(rule)) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            DSTextButton(onClick = { onEvent(FinanceEvent.HideIgnorePatternsDialog) }) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun PatternItem(
    pattern: String,
    onRemove: () -> Unit
) {
    DSCardCompact(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = DSColors.expense,
                    modifier = Modifier.size(18.dp)
                )
                DSText(
                    text = pattern,
                    variant = TextVariant.BODY
                )
            }
            
            DSIconButton(
                onClick = onRemove,
                icon = Icons.Default.Close,
                contentDescription = "Remover",
                size = ComponentSize.SM
            )
        }
    }
}

@Composable
private fun AdvancedRuleItem(
    rule: com.financehub.domain.model.AdvancedIgnoreRule,
    onRemove: () -> Unit
) {
    DSCard(variant = CardVariant.WARNING) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = rule.pattern,
                        variant = TextVariant.TITLE
                    )
                    if (rule.description.isNotBlank()) {
                        DSText(
                            text = rule.description,
                            variant = TextVariant.CAPTION
                        )
                    }
                }
                
                DSIconButton(
                    onClick = onRemove,
                    icon = Icons.Default.Close,
                    contentDescription = "Remover",
                    size = ComponentSize.SM
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSSpacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DSColors.warning
                    )
                    DSText(
                        text = "≥ R$ ${String.format("%.0f", rule.minAmount)}",
                        variant = TextVariant.CAPTION
                    )
                }
                
                if (rule.requiresIntegerValue) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DSSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        DSText(
                            text = "Redondo",
                            variant = TextVariant.CAPTION
                        )
                    }
                }
            }
        }
    }
}
