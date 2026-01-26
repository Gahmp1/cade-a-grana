package com.financehub

import androidx.compose.foundation.background
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
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*
import com.financehub.domain.model.Household
import com.financehub.domain.model.MemberSummary
import com.financehub.domain.model.Transaction
import com.financehub.presentation.FinanceEvent
import com.financehub.presentation.FinanceState
import kotlin.math.abs

/**
 * Dashboard de Saúde da Casa - Visão conjunta das finanças
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdDashboard(
    state: FinanceState,
    onEvent: (FinanceEvent) -> Unit
) {
    val household = state.household
    val transactions = state.transactions.filter { !it.isIgnored }
    
    // Calcular resumos por membro
    val memberSummaries = calculateMemberSummaries(transactions, household)
    val totalSummary = calculateTotalSummary(memberSummaries)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSText(
                text = "🏠 Saúde da ${household.name}",
                variant = TextVariant.HEADLINE
            )
            
            DSIconButton(
                onClick = { onEvent(FinanceEvent.ToggleHouseholdView) },
                icon = Icons.Default.Close,
                contentDescription = "Fechar"
            )
        }
        
        // Resumo Total da Casa
        HouseholdTotalCard(totalSummary)
        
        // Resumos por membro
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(memberSummaries) { summary ->
                MemberSummaryCard(summary, household)
            }
        }
    }
}

@Composable
private fun HouseholdTotalCard(summary: MemberSummary) {
    DSCard(
        variant = if (summary.balance >= 0) CardVariant.SUCCESS else CardVariant.WARNING
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.md)
        ) {
            DSText(
                text = "💰 Resumo Geral",
                variant = TextVariant.HEADLINE
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DSText(
                        text = "Entradas",
                        variant = TextVariant.LABEL
                    )
                    DSMoneyText(
                        amount = summary.totalIncome,
                        isIncome = true,
                        size = MoneyTextSize.LARGE
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DSText(
                        text = "Saídas",
                        variant = TextVariant.LABEL
                    )
                    DSMoneyText(
                        amount = summary.totalExpense,
                        isIncome = false,
                        size = MoneyTextSize.LARGE
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DSText(
                        text = "Saldo",
                        variant = TextVariant.LABEL
                    )
                    DSMoneyText(
                        amount = summary.balance,
                        isIncome = summary.balance >= 0,
                        size = MoneyTextSize.LARGE
                    )
                }
            }
            
            DSText(
                text = "Total: ${summary.transactionCount} transações",
                variant = TextVariant.CAPTION
            )
        }
    }
}

@Composable
private fun MemberSummaryCard(summary: MemberSummary, household: Household) {
    val memberName = summary.member?.name ?: household.sharedLabel
    val memberColor = summary.member?.colorHex?.let { parseColorHex(it) } ?: Color.Gray
    
    DSCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.DEFAULT
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(memberColor, DSRadius.sm)
                    )
                    DSText(
                        text = memberName,
                        variant = TextVariant.TITLE
                    )
                }
                
                DSMoneyText(
                    amount = summary.balance,
                    isIncome = summary.balance >= 0,
                    size = MoneyTextSize.MEDIUM
                )
            }
            
            HorizontalDivider(color = DSColors.Dark.borderSubtle)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    DSText(text = "Entradas", variant = TextVariant.LABEL)
                    DSMoneyText(amount = summary.totalIncome, isIncome = true)
                }
                
                Column {
                    DSText(text = "Saídas", variant = TextVariant.LABEL)
                    DSMoneyText(amount = summary.totalExpense, isIncome = false)
                }
                
                Column {
                    DSText(text = "Transações", variant = TextVariant.LABEL)
                    DSText(
                        text = summary.transactionCount.toString(),
                        variant = TextVariant.BODY
                    )
                }
            }
            
            // Top 3 categorias
            if (summary.categories.isNotEmpty()) {
                HorizontalDivider(color = DSColors.Dark.borderSubtle)
                DSText(
                    text = "Top 3 Categorias de Gastos:",
                    variant = TextVariant.LABEL
                )
                
                summary.categories.entries
                    .sortedByDescending { it.value }
                    .take(3)
                    .forEach { (category, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DSText(
                                text = category.displayName,
                                variant = TextVariant.CAPTION
                            )
                            DSMoneyText(
                                amount = amount,
                                isIncome = false,
                                size = MoneyTextSize.SMALL
                            )
                        }
                    }
            }
        }
    }
}

/**
 * Calcula resumos por membro
 */
private fun calculateMemberSummaries(
    transactions: List<Transaction>,
    household: Household
): List<MemberSummary> {
    val summaries = mutableListOf<MemberSummary>()
    
    // Resumo por cada membro
    household.members.forEach { member ->
        val memberTransactions = transactions.filter { it.owner == member.name }
        summaries.add(createMemberSummary(member.name, memberTransactions, member))
    }
    
    // Resumo das transações compartilhadas
    val sharedTransactions = transactions.filter { it.owner == household.sharedLabel }
    if (sharedTransactions.isNotEmpty()) {
        summaries.add(createMemberSummary(null, sharedTransactions, null))
    }
    
    // Resumo das transações não atribuídas
    val unassignedTransactions = transactions.filter { it.owner == null }
    if (unassignedTransactions.isNotEmpty()) {
        summaries.add(createMemberSummary("Não atribuído", unassignedTransactions, null))
    }
    
    return summaries
}

private fun createMemberSummary(
    ownerName: String?,
    transactions: List<Transaction>,
    member: com.financehub.domain.model.HouseholdMember?
): MemberSummary {
    val income = transactions.filter { it.isIncome }.sumOf { it.amount }
    val expense = transactions.filter { it.isExpense }.sumOf { abs(it.amount) }
    
    val categories = transactions
        .filter { it.isExpense }
        .groupBy { it.category }
        .mapValues { (_, txs) -> txs.sumOf { abs(it.amount) } }
    
    return MemberSummary(
        member = member,
        totalIncome = income,
        totalExpense = expense,
        balance = income - expense,
        transactionCount = transactions.size,
        categories = categories
    )
}

private fun calculateTotalSummary(summaries: List<MemberSummary>): MemberSummary {
    return MemberSummary(
        member = null,
        totalIncome = summaries.sumOf { it.totalIncome },
        totalExpense = summaries.sumOf { it.totalExpense },
        balance = summaries.sumOf { it.balance },
        transactionCount = summaries.sumOf { it.transactionCount },
        categories = emptyMap()
    )
}

/**
 * Parse hex color string to Compose Color
 */
private fun parseColorHex(hex: String): Color {
    val hexColor = hex.removePrefix("#")
    return Color(hexColor.toLong(16) or 0xFF000000)
}
