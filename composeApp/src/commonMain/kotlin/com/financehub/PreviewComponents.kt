package com.financehub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financehub.domain.model.Category
import com.financehub.domain.model.Transaction
import com.financehub.presentation.FinanceEvent
import com.financehub.presentation.FinanceState
import com.financehub.theme.FinanceButtonStyles
import java.io.File
import kotlin.math.abs

/**
 * Tela de seleção de arquivos para preview
 */
@Composable
fun FileSelectionScreen(
    state: FinanceState,
    onEvent: (FinanceEvent) -> Unit,
    onSelectFiles: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Selecionar Arquivos para Preview",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Como usar:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("1. Clique no botão abaixo para selecionar arquivos CSV ou PDF")
                Text("2. Revise a lista de arquivos selecionados")
                Text("3. Clique em 'Fazer Preview' para visualizar as transações")
                Text("4. Edite categorias ou tipos de transação conforme necessário")
                Text("5. Confirme para processar ou cancele para voltar")
            }
        }
        
        Button(
            onClick = onSelectFiles,
            modifier = Modifier.fillMaxWidth(),
            colors = FinanceButtonStyles.primaryButtonColors()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Selecionar Arquivos (CSV/PDF)")
        }
        
        if (state.selectedFiles.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Arquivos Selecionados (${state.selectedFiles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.selectedFiles) { filePath ->
                            FileItem(filePath)
                        }
                    }
                }
            }
            
            Button(
                onClick = { onEvent(FinanceEvent.PreviewSelectedFiles) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                colors = FinanceButtonStyles.primaryButtonColors()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Processando...")
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Fazer Preview das Transações")
                }
            }
        }
        
        if (state.error != null) {
            ErrorCard(
                message = state.error ?: "",
                onDismiss = { onEvent(FinanceEvent.ClearError) }
            )
        }
    }
}

@Composable
private fun FileItem(filePath: String) {
    val file = File(filePath)
    val fileName = file.name
    val fileSize = file.length() / 1024 // KB
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.small
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${fileSize}KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Tela de preview e edição de transações
 */
@Composable
fun TransactionPreviewScreen(
    state: FinanceState,
    onEvent: (FinanceEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header com estatísticas
        PreviewHeader(state)
        
        // Barra de pesquisa
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { onEvent(FinanceEvent.UpdateSearchQuery(it)) }
        )
        
        // Lista de transações editáveis
        val filteredTransactions = if (state.searchQuery.isBlank()) {
            state.pendingTransactions
        } else {
            state.pendingTransactions.filter {
                it.description.contains(state.searchQuery, ignoreCase = true) ||
                it.category.displayName.contains(state.searchQuery, ignoreCase = true)
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTransactions, key = { it.id }) { transaction ->
                EditableTransactionCard(
                    transaction = transaction,
                    onCategoryChange = { newCategory ->
                        onEvent(FinanceEvent.EditTransactionCategory(transaction.id, newCategory))
                    },
                    onToggleType = {
                        onEvent(FinanceEvent.ToggleTransactionType(transaction.id))
                    }
                )
            }
        }
        
        // Botões de ação
        PreviewActions(
            state = state,
            onConfirm = { onEvent(FinanceEvent.ConfirmPreview) },
            onCancel = { onEvent(FinanceEvent.CancelPreview) },
            onExportLog = { onEvent(FinanceEvent.ExportChangeLog) }
        )
    }
}

@Composable
private fun PreviewHeader(state: FinanceState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Preview de Transações",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "${state.pendingTransactions.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Transações", style = MaterialTheme.typography.bodySmall)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${state.changeLog.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (state.changeLog.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Unspecified
                    )
                    Text("Editadas", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            if (state.changeLog.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Você fez ${state.changeLog.size} alteração(ões). Use 'Exportar Log' para salvar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por descrição ou categoria...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun EditableTransactionCard(
    transaction: Transaction,
    onCategoryChange: (Category) -> Unit,
    onToggleType: () -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Linha 1: Descrição e valor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        transaction.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "R$ ${String.format("%.2f", abs(transaction.amount))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.isIncome) 
                            MaterialTheme.colorScheme.tertiary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                    Text(
                        transaction.source.displayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            HorizontalDivider()
            
            // Linha 2: Controles de edição
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de tipo (Entrada/Saída)
                FilterChip(
                    selected = false,
                    onClick = onToggleType,
                    label = {
                        Text(if (transaction.isIncome) "Entrada" else "Saída")
                    },
                    leadingIcon = {
                        Icon(
                            if (transaction.isIncome) Icons.Default.Add else Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                )
                
                // Dropdown de categoria
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { showCategoryMenu = true },
                        label = { Text(transaction.category.displayName) },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        Category.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    onCategoryChange(category)
                                    showCategoryMenu = false
                                },
                                leadingIcon = {
                                    if (category == transaction.category) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewActions(
    state: FinanceState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onExportLog: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.changeLog.isNotEmpty()) {
            OutlinedButton(
                onClick = onExportLog,
                modifier = Modifier.fillMaxWidth(),
                colors = FinanceButtonStyles.outlinedButtonColors()
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Log de Mudanças")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = FinanceButtonStyles.outlinedNegativeButtonColors()
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Cancelar")
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = FinanceButtonStyles.positiveButtonColors()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Confirmar e Processar")
            }
        }
    }
}
