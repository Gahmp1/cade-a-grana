package com.financehub

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*
import com.financehub.domain.model.ConsolidatedReport
import com.financehub.domain.model.MonthReport
import kotlin.math.abs

@Composable
fun ConsolidatedSummaryCard(report: ConsolidatedReport) {
    DSCard(
        variant = CardVariant.DEFAULT,
        elevation = DSElevation.md
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            DSText(
                text = "Resumo Consolidado",
                variant = TextVariant.TITLE
            )
            
            HorizontalDivider(color = DSColors.Dark.borderSubtle)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Período:", variant = TextVariant.BODY, fontWeight = FontWeight.Medium)
                DSText(text = "${report.startMonth} até ${report.endMonth}", variant = TextVariant.BODY)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Total de meses:", variant = TextVariant.BODY, fontWeight = FontWeight.Medium)
                DSText(text = "${report.monthCount}", variant = TextVariant.BODY)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Transações:", variant = TextVariant.BODY, fontWeight = FontWeight.Medium)
                DSText(text = "${report.totalTransactions}", variant = TextVariant.BODY)
            }
            
            HorizontalDivider(color = DSColors.Dark.borderSubtle)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Total Receitas:", variant = TextVariant.BODY, fontWeight = FontWeight.Medium)
                DSMoneyText(amount = report.totalIncome, isIncome = true)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Total Despesas:", variant = TextVariant.BODY, fontWeight = FontWeight.Medium)
                DSMoneyText(amount = report.totalExpense, isIncome = false)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Saldo:", variant = TextVariant.BODY, fontWeight = FontWeight.Bold)
                DSMoneyText(
                    amount = report.balance,
                    isIncome = report.balance >= 0,
                    size = MoneyTextSize.MEDIUM
                )
            }
            
            HorizontalDivider(color = DSColors.Dark.borderSubtle)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Média Mensal Receitas:", variant = TextVariant.BODY, fontWeight = FontWeight.Medium)
                DSText(text = "R$ ${String.format("%.2f", report.averageMonthlyIncome)}", variant = TextVariant.BODY)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(text = "Média Mensal Despesas:", variant = TextVariant.BODY, fontWeight = FontWeight.Medium)
                DSText(text = "R$ ${String.format("%.2f", report.averageMonthlyExpense)}", variant = TextVariant.BODY)
            }
        }
    }
}

@Composable
fun MonthReportCard(
    monthReport: MonthReport,
    isSelected: Boolean,
    onClick: () -> Unit,
    onViewDetails: (() -> Unit)? = null
) {
    DSCard(
        onClick = onClick,
        variant = if (isSelected) CardVariant.SELECTED else CardVariant.DEFAULT,
        elevation = DSElevation.sm
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = monthReport.yearMonth,
                    variant = TextVariant.TITLE
                )
                
                DSText(
                    text = "${monthReport.sourceCount} fonte(s) - ${monthReport.transactionCount} transações",
                    variant = TextVariant.CAPTION
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    DSText(text = "Receitas", variant = TextVariant.LABEL)
                    DSMoneyText(amount = monthReport.totalIncome, isIncome = true)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    DSText(text = "Despesas", variant = TextVariant.LABEL)
                    DSMoneyText(amount = monthReport.totalExpense, isIncome = false)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    DSText(text = "Saldo", variant = TextVariant.LABEL)
                    DSMoneyText(
                        amount = monthReport.balance,
                        isIncome = monthReport.balance >= 0,
                        size = MoneyTextSize.MEDIUM
                    )
                }
            }
            
            if (isSelected) {
                HorizontalDivider(color = DSColors.Dark.borderSubtle)
                
                // Botão Ver Detalhes
                if (onViewDetails != null) {
                    DSButton(
                        onClick = onViewDetails,
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Default.Check
                    ) {
                        Text("Ver Detalhes do Mês")
                    }
                    
                    HorizontalDivider(color = DSColors.Dark.borderSubtle)
                }
                
                DSText(
                    text = "Categorias",
                    variant = TextVariant.TITLE
                )
                
                monthReport.categorySummary.values.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DSText(
                            text = category.category.displayName,
                            variant = TextVariant.CAPTION
                        )
                        DSMoneyText(
                            amount = abs(category.expense),
                            isIncome = false,
                            size = MoneyTextSize.SMALL
                        )
                    }
                }
            }
        }
    }
}
