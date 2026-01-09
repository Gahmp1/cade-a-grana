package com.financehub.domain.model

/**
 * Relatório consolidado de todos os meses
 */
data class ConsolidatedReport(
    val monthReports: List<MonthReport>,
    val startMonth: String,
    val endMonth: String
) {
    val totalIncome: Double = monthReports.sumOf { it.totalIncome }
    val totalExpense: Double = monthReports.sumOf { it.totalExpense }
    val balance: Double = totalIncome - totalExpense
    val totalTransactions: Int = monthReports.sumOf { it.transactionCount }
    val monthCount: Int = monthReports.size
    
    val averageMonthlyIncome: Double = if (monthCount > 0) totalIncome / monthCount else 0.0
    val averageMonthlyExpense: Double = if (monthCount > 0) totalExpense / monthCount else 0.0
    
    /**
     * Resumo consolidado por categoria
     */
    val categorySummary: Map<Category, CategorySummary> by lazy {
        val allTransactions = monthReports.flatMap { it.transactions }
        Category.values().associateWith { category ->
            val categoryTransactions = allTransactions.filter { it.category == category }
            CategorySummary(
                category = category,
                transactions = categoryTransactions,
                income = categoryTransactions.filter { it.isIncome }.sumOf { it.amount },
                expense = categoryTransactions.filter { it.isExpense }.sumOf { kotlin.math.abs(it.amount) }
            )
        }.filterValues { it.transactionCount > 0 }
    }
}
