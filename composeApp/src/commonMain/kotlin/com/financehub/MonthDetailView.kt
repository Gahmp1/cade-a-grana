package com.financehub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*
import com.financehub.domain.model.MonthReport
import com.financehub.domain.model.Transaction
import com.financehub.presentation.FinanceEvent
import com.financehub.presentation.FinanceState
import kotlin.math.abs

/**
 * Visão detalhada de um mês específico com entradas e saídas separadas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailView(
    yearMonth: String,
    transactions: List<Transaction>,
    household: com.financehub.domain.model.Household,
    onEvent: (FinanceEvent) -> Unit,
    onClose: () -> Unit
) {
    val (year, month) = yearMonth.split("-")
    val monthName = when (month.toInt()) {
        1 -> "Janeiro"; 2 -> "Fevereiro"; 3 -> "Março"
        4 -> "Abril"; 5 -> "Maio"; 6 -> "Junho"
        7 -> "Julho"; 8 -> "Agosto"; 9 -> "Setembro"
        10 -> "Outubro"; 11 -> "Novembro"; 12 -> "Dezembro"
        else -> month
    }
    
    val activeTransactions = transactions.filter { !it.isIgnored }
    val incomeTransactions = activeTransactions.filter { it.isIncome }.sortedByDescending { it.date }
    val expenseTransactions = activeTransactions.filter { it.isExpense }.sortedByDescending { it.date }
    
    val totalIncome = incomeTransactions.sumOf { it.amount }
    val totalExpense = expenseTransactions.sumOf { abs(it.amount) }
    val balance = totalIncome - totalExpense
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$monthName $year - Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumo do mês
            DSCard(
                variant = if (balance >= 0) CardVariant.SUCCESS else CardVariant.WARNING
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
                ) {
                    DSText(
                        text = "Resumo do Mês",
                        variant = TextVariant.HEADLINE
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            DSText(text = "Entradas", variant = TextVariant.LABEL)
                            DSMoneyText(
                                amount = totalIncome,
                                isIncome = true,
                                size = MoneyTextSize.MEDIUM
                            )
                        }
                        
                        Column {
                            DSText(text = "Saídas", variant = TextVariant.LABEL)
                            DSMoneyText(
                                amount = totalExpense,
                                isIncome = false,
                                size = MoneyTextSize.MEDIUM
                            )
                        }
                        
                        Column {
                            DSText(text = "Saldo", variant = TextVariant.LABEL)
                            DSMoneyText(
                                amount = balance,
                                isIncome = balance >= 0,
                                size = MoneyTextSize.MEDIUM
                            )
                        }
                    }
                }
            }
            
            // Lista de transações separadas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seção de Entradas
                item {
                    DSText(
                        text = "💰 Entradas (${incomeTransactions.size})",
                        variant = TextVariant.INCOME_LARGE
                    )
                }
                
                items(incomeTransactions) { transaction ->
                    TransactionDetailCard(
                        transaction = transaction,
                        household = household,
                        onToggleIgnored = {
                            onEvent(FinanceEvent.ToggleTransactionIgnored(transaction.id))
                        },
                        onSetOwner = { owner ->
                            onEvent(FinanceEvent.SetTransactionOwner(transaction.id, owner))
                        }
                    )
                }
                
                // Seção de Saídas
                item {
                    Spacer(Modifier.height(DSSpacing.md))
                    DSText(
                        text = "💸 Saídas (${expenseTransactions.size})",
                        variant = TextVariant.EXPENSE_LARGE
                    )
                }
                
                items(expenseTransactions) { transaction ->
                    TransactionDetailCard(
                        transaction = transaction,
                        household = household,
                        onToggleIgnored = {
                            onEvent(FinanceEvent.ToggleTransactionIgnored(transaction.id))
                        },
                        onSetOwner = { owner ->
                            onEvent(FinanceEvent.SetTransactionOwner(transaction.id, owner))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailCard(
    transaction: Transaction,
    household: com.financehub.domain.model.Household,
    onToggleIgnored: () -> Unit,
    onSetOwner: (String?) -> Unit
) {
    var showOwnerMenu by remember { mutableStateOf(false) }
    
    DSCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (transaction.isIgnored) CardVariant.MUTED else CardVariant.DEFAULT
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DSText(
                    text = transaction.description,
                    variant = TextVariant.BODY
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSText(
                        text = transaction.date.toString(),
                        variant = TextVariant.CAPTION
                    )
                    
                    DSCategoryBadge(categoryName = transaction.category.displayName)
                    
                    // Owner badge
                    transaction.owner?.let { owner ->
                        val member = household.getMember(owner)
                        DSOwnerBadge(
                            ownerName = owner,
                            ownerColor = member?.colorHex?.let { parseColorHex(it) } ?: Color.Gray
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                DSMoneyText(
                    amount = abs(transaction.amount),
                    isIncome = transaction.isIncome,
                    size = MoneyTextSize.MEDIUM
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.xs)) {
                    // Botão de atribuir owner
                    DSIconButton(
                        onClick = { showOwnerMenu = true },
                        icon = Icons.Default.Person,
                        contentDescription = "Atribuir dono",
                        size = ComponentSize.SM
                    )
                    
                    // Botão de ignorar
                    DSIconButton(
                        onClick = onToggleIgnored,
                        icon = if (transaction.isIgnored) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (transaction.isIgnored) "Incluir" else "Ignorar",
                        size = ComponentSize.SM,
                        variant = if (transaction.isIgnored) ButtonVariant.NEGATIVE else ButtonVariant.GHOST
                    )
                }
            }
        }
        
        // Dropdown menu para selecionar owner
        DropdownMenu(
            expanded = showOwnerMenu,
            onDismissRequest = { showOwnerMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Sem atribuição") },
                onClick = {
                    onSetOwner(null)
                    showOwnerMenu = false
                }
            )
            
            household.members.forEach { member ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        parseColorHex(member.colorHex),
                                        DSRadius.xs
                                    )
                            )
                            Spacer(Modifier.width(DSSpacing.sm))
                            Text(member.name)
                        }
                    },
                    onClick = {
                        onSetOwner(member.name)
                        showOwnerMenu = false
                    }
                )
            }
            
            DropdownMenuItem(
                text = { Text(household.sharedLabel) },
                onClick = {
                    onSetOwner(household.sharedLabel)
                    showOwnerMenu = false
                }
            )
        }
    }
}

/**
 * Parse hex color string to Compose Color
 */
private fun parseColorHex(hex: String): Color {
    val hexColor = hex.removePrefix("#")
    return Color(hexColor.toLong(16) or 0xFF000000)
}
