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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*
import com.financehub.domain.model.Category
import com.financehub.domain.model.Transaction
import com.financehub.presentation.FinanceEvent
import com.financehub.presentation.FinanceState
import java.io.File
import kotlin.math.abs

/**
 * Tela inicial (Home) - seleção de arquivos para preview
 */
@Composable
fun HomeScreen(
    state: FinanceState,
    onEvent: (FinanceEvent) -> Unit,
    onSelectFiles: () -> Unit
) {
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header com título e botão de limpar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSText(
                text = "FinanceHub - Home",
                variant = TextVariant.HEADLINE
            )
            
            if (state.savedMonths.isNotEmpty()) {
                DSButton(
                    onClick = { showClearDataDialog = true },
                    variant = ButtonVariant.NEGATIVE,
                    leadingIcon = Icons.Default.Delete
                ) {
                    Text("Limpar Dados")
                }
            }
        }
        
        DSCard(variant = CardVariant.ELEVATED) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
            ) {
                DSText(
                    text = "Como usar:",
                    variant = TextVariant.TITLE
                )
                DSText(text = "1. Clique no botão abaixo para selecionar arquivos CSV ou PDF")
                DSText(text = "2. Revise a lista de arquivos selecionados")
                DSText(text = "3. Clique em 'Fazer Preview' para visualizar as transações")
                DSText(text = "4. Edite categorias ou tipos de transação conforme necessário")
                DSText(text = "5. Confirme para processar ou cancele para voltar")
            }
        }
        
        DSButton(
            onClick = onSelectFiles,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.PRIMARY,
            leadingIcon = Icons.Default.Add
        ) {
            Text("Selecionar Arquivos (CSV/PDF)")
        }
        
        // Histórico de dados salvos
        if (state.savedMonths.isNotEmpty()) {
            DSCard(variant = CardVariant.INFO) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DSText(
                            text = "📂 Histórico Salvo (${state.savedMonths.size} meses)",
                            variant = TextVariant.TITLE
                        )
                        
                        DSButton(
                            onClick = { onEvent(FinanceEvent.LoadHistoricalData) },
                            enabled = state.transactions.isEmpty(),
                            variant = ButtonVariant.SECONDARY
                        ) {
                            Text("Carregar Tudo")
                        }
                    }
                    
                    DSText(
                        text = state.savedMonths.joinToString(", "),
                        variant = TextVariant.BODY
                    )
                    
                    DSText(
                        text = "Os dados salvos serão mesclados automaticamente ao processar novos arquivos",
                        variant = TextVariant.CAPTION
                    )
                }
            }
        }
        
        if (state.selectedFiles.isNotEmpty()) {
            DSCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
                ) {
                    DSText(
                        text = "Arquivos Selecionados (${state.selectedFiles.size})",
                        variant = TextVariant.TITLE
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(DSSpacing.xs)
                    ) {
                        items(state.selectedFiles) { filePath ->
                            FileItem(filePath)
                        }
                    }
                }
            }
            
            DSButton(
                onClick = { onEvent(FinanceEvent.PreviewSelectedFiles) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                variant = ButtonVariant.PRIMARY,
                leadingIcon = if (state.isLoading) null else Icons.Default.Check
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(DSSpacing.sm))
                    Text("Processando...")
                } else {
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
    
    // Dialog de confirmação para limpar dados
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { DSText(text = "Limpar Todos os Dados?", variant = TextVariant.TITLE) },
            text = {
                Column {
                    DSText(
                        text = "Esta ação irá remover TODOS os dados salvos:",
                        variant = TextVariant.BODY
                    )
                    Spacer(Modifier.height(DSSpacing.sm))
                    DSText(text = "• ${state.savedMonths.size} meses de transações")
                    DSText(text = "• Todos os arquivos em ~/.financehub/transactions/")
                    Spacer(Modifier.height(DSSpacing.sm))
                    DSText(
                        text = "Esta ação NÃO PODE ser desfeita!",
                        variant = TextVariant.EXPENSE
                    )
                }
            },
            confirmButton = {
                DSButton(
                    onClick = {
                        onEvent(FinanceEvent.ClearAllData)
                        showClearDataDialog = false
                    },
                    variant = ButtonVariant.NEGATIVE
                ) {
                    Text("Sim, Limpar Tudo")
                }
            },
            dismissButton = {
                DSTextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                DSColors.Dark.surfaceElevated,
                DSRadius.sm
            )
            .padding(DSSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = DSColors.primary
            )
            Column {
                DSText(
                    text = fileName,
                    variant = TextVariant.BODY
                )
                DSText(
                    text = "${fileSize}KB",
                    variant = TextVariant.CAPTION
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
        
        // Botão de gerenciar padrões
        DSButton(
            onClick = { onEvent(FinanceEvent.ShowIgnorePatternsDialog) },
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.SECONDARY,
            leadingIcon = Icons.Default.Settings
        ) {
            Text("Gerenciar Padrões de Ignorar (${state.ignorePatterns.patterns.size})")
        }
        
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
                    },
                    onIgnoreToggle = {
                        onEvent(FinanceEvent.ToggleTransactionIgnored(transaction.id))
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
    
    // Dialog de padrões de ignorar
    if (state.showIgnorePatternsDialog) {
        IgnorePatternsDialog(
            state = state,
            onEvent = onEvent
        )
    }
}

@Composable
private fun PreviewHeader(state: FinanceState) {
    DSCard(variant = CardVariant.ELEVATED) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            DSText(
                text = "Preview de Transações",
                variant = TextVariant.HEADLINE
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    DSText(
                        text = "${state.pendingTransactions.size}",
                        variant = TextVariant.DISPLAY
                    )
                    DSText(text = "Transações", variant = TextVariant.CAPTION)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DSText(
                        text = "${state.pendingTransactions.count { it.isIgnored }}",
                        variant = if (state.pendingTransactions.any { it.isIgnored }) TextVariant.EXPENSE else TextVariant.DISPLAY
                    )
                    DSText(text = "Ignoradas", variant = TextVariant.CAPTION)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DSText(
                        text = "${state.pendingTransactions.count { it.needsReview }}",
                        variant = if (state.pendingTransactions.any { it.needsReview }) TextVariant.EXPENSE else TextVariant.DISPLAY
                    )
                    DSText(text = "Revisão", variant = TextVariant.CAPTION)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    DSText(
                        text = "${state.changeLog.size}",
                        variant = if (state.changeLog.isNotEmpty()) TextVariant.INCOME else TextVariant.DISPLAY
                    )
                    DSText(text = "Editadas", variant = TextVariant.CAPTION)
                }
            }
            
            if (state.changeLog.isNotEmpty()) {
                HorizontalDivider(color = DSColors.Dark.borderSubtle)
                DSText(
                    text = "Você fez ${state.changeLog.size} alteração(ões). Use 'Exportar Log' para salvar.",
                    variant = TextVariant.CAPTION
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
    DSSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = "Buscar por descrição ou categoria...",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EditableTransactionCard(
    transaction: Transaction,
    onCategoryChange: (Category) -> Unit,
    onToggleType: () -> Unit,
    onIgnoreToggle: () -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    DSCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (transaction.isIgnored && !transaction.needsReview) 0.5f else 1f),
        variant = if (transaction.needsReview) CardVariant.WARNING else CardVariant.DEFAULT,
        elevation = DSElevation.sm
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            // Badge de revisão
            if (transaction.needsReview) {
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
                        text = "NECESSITA REVISÃO - Regra Avançada",
                        variant = TextVariant.LABEL
                    )
                }
            }
            
            // Linha 1: Descrição e valor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = transaction.description,
                        variant = TextVariant.BODY
                    )
                    DSText(
                        text = transaction.date.toString(),
                        variant = TextVariant.CAPTION
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    DSMoneyText(
                        amount = abs(transaction.amount),
                        isIncome = transaction.isIncome,
                        size = MoneyTextSize.MEDIUM
                    )
                    DSText(
                        text = transaction.source.displayName,
                        variant = TextVariant.CAPTION
                    )
                }
            }
            
            HorizontalDivider(color = DSColors.Dark.borderSubtle)
            
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
                
                Spacer(Modifier.weight(1f))
                
                // Botão de ignorar
                FilterChip(
                    selected = transaction.isIgnored,
                    onClick = onIgnoreToggle,
                    label = { Text(if (transaction.isIgnored) "Ignorada" else "Ignorar") },
                    leadingIcon = {
                        Icon(
                            if (transaction.isIgnored) Icons.Default.Close else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                )
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
        verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
    ) {
        if (state.changeLog.isNotEmpty()) {
            DSButton(
                onClick = onExportLog,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.SECONDARY,
                leadingIcon = Icons.Default.Info
            ) {
                Text("Exportar Log de Mudanças")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            DSButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.NEGATIVE,
                leadingIcon = Icons.Default.Close
            ) {
                Text("Cancelar")
            }
            
            DSButton(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.POSITIVE,
                leadingIcon = Icons.Default.CheckCircle
            ) {
                Text("Confirmar e Processar")
            }
        }
    }
}
