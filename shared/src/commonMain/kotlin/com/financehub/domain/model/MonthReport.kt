package com.financehub.domain.model

import kotlinx.datetime.LocalDate

/**
 * Relatório mensal de transações
 */
data class MonthReport(
    val yearMonth: String, // formato: "2025-01"
    val transactions: List<Transaction>,
    val dataSources: Set<DataSource>
) {
    val totalIncome: Double = transactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense: Double = transactions.filter { it.isExpense }.sumOf { kotlin.math.abs(it.amount) }
    val balance: Double = totalIncome - totalExpense
    val transactionCount: Int = transactions.size
    val sourceCount: Int = dataSources.size
    
    /**
     * Agrupa transações por categoria
     */
    val categorySummary: Map<Category, CategorySummary> by lazy {
        Category.values().associateWith { category ->
            val categoryTransactions = transactions.filter { it.category == category }
            CategorySummary(
                category = category,
                transactions = categoryTransactions,
                income = categoryTransactions.filter { it.isIncome }.sumOf { it.amount },
                expense = categoryTransactions.filter { it.isExpense }.sumOf { kotlin.math.abs(it.amount) }
            )
        }.filterValues { it.transactionCount > 0 }
    }
    
    /**
     * Agrupa transações por fonte de dados (breakdown por banco)
     */
    val sourceBreakdown: Map<String, SourceSummary> by lazy {
        val sources = transactions.map { it.effectiveSourceName }.distinct()
        sources.associateWith { sourceName ->
            val sourceTransactions = transactions.filter { it.effectiveSourceName == sourceName }
            SourceSummary(
                sourceName = sourceName,
                transactions = sourceTransactions,
                totalIncome = sourceTransactions.filter { it.isIncome }.sumOf { it.amount },
                totalExpense = sourceTransactions.filter { it.isExpense }.sumOf { kotlin.math.abs(it.amount) }
            )
        }.toSortedMap(compareByDescending { sourceBreakdown[it]?.totalExpense ?: 0.0 })
    }
    
    /**
     * Top 5 maiores gastos
     */
    val topExpenses: List<Transaction> by lazy {
        transactions
            .filter { it.isExpense }
            .sortedByDescending { kotlin.math.abs(it.amount) }
            .take(5)
    }
    
    /**
     * Top 5 maiores receitas
     */
    val topIncomes: List<Transaction> by lazy {
        transactions
            .filter { it.isIncome }
            .sortedByDescending { it.amount }
            .take(5)
    }
}

/**
 * Resumo de uma categoria
 */
data class CategorySummary(
    val category: Category,
    val transactions: List<Transaction>,
    val income: Double,
    val expense: Double
) {
    val balance: Double = income - expense
    val transactionCount: Int = transactions.size
    val incomeCount: Int = transactions.count { it.isIncome }
    val expenseCount: Int = transactions.count { it.isExpense }
    
    val topTransactions: List<Transaction> by lazy {
        transactions
            .sortedByDescending { kotlin.math.abs(it.amount) }
            .take(if (transactions.size <= 5) transactions.size else 3)
    }
}

/**
 * Resumo de uma fonte de dados (banco/cartão)
 */
data class SourceSummary(
    val sourceName: String, // Nome customizado ou DataSource.displayName
    val transactions: List<Transaction>,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val balance: Double = totalIncome - totalExpense
    val transactionCount: Int = transactions.size
    val incomeCount: Int = transactions.count { it.isIncome }
    val expenseCount: Int = transactions.count { it.isExpense }
}
