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
                Text(
                    "Transações contendo essas palavras serão automaticamente marcadas como ignoradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Seção: Padrões Simples
                Text(
                    "Padrões Simples",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Campo para adicionar novo padrão
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ex: aporte, resgate...") },
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (newPattern.isNotBlank()) {
                                onEvent(FinanceEvent.AddIgnorePattern(newPattern))
                                newPattern = ""
                            }
                        },
                        enabled = newPattern.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }
                
                // Lista de padrões simples
                if (state.ignorePatterns.patterns.isEmpty()) {
                    Text(
                        "Nenhum padrão configurado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
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
                    Text(
                        "Regras Avançadas (${state.ignorePatterns.advancedRules.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { showAdvancedSection = !showAdvancedSection }) {
                        Icon(
                            if (showAdvancedSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
                
                if (showAdvancedSection) {
                    Text(
                        "Combine padrão de texto + condições de valor. Transações marcadas aparecem para revisão.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Formulário de regra avançada
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = advPattern,
                                onValueChange = { advPattern = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Padrão de texto") },
                                placeholder = { Text("Ex: pix recebido") },
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = advMinAmount,
                                onValueChange = { advMinAmount = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Valor mínimo (R$)") },
                                placeholder = { Text("3000") },
                                singleLine = true
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = advRequiresInteger,
                                    onCheckedChange = { advRequiresInteger = it }
                                )
                                Text("Apenas valores redondos (múltiplos de 1000)")
                            }
                            
                            OutlinedTextField(
                                value = advDescription,
                                onValueChange = { advDescription = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Descrição (opcional)") },
                                placeholder = { Text("Ex: Transferências grandes") },
                                singleLine = true
                            )
                            
                            Button(
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
                                enabled = advPattern.isNotBlank() && advMinAmount.toDoubleOrNull() != null
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
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
            TextButton(onClick = { onEvent(FinanceEvent.HideIgnorePatternsDialog) }) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    pattern,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remover",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AdvancedRuleItem(
    rule: com.financehub.domain.model.AdvancedIgnoreRule,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rule.pattern,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (rule.description.isNotBlank()) {
                        Text(
                            rule.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remover",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "≥ R$ ${String.format("%.0f", rule.minAmount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (rule.requiresIntegerValue) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Redondo",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
