package com.financehub.domain.model

import kotlinx.datetime.LocalDate

/**
 * Representa uma transação financeira
 */
data class Transaction(
    val id: String,
    val date: LocalDate,
    val description: String,
    val amount: Double, // Positivo = Entrada, Negativo = Saída
    val category: Category,
    val source: DataSource,
    val rawData: String = ""
) {
    val isIncome: Boolean get() = amount > 0
    val isExpense: Boolean get() = amount < 0
    val absAmount: Double get() = kotlin.math.abs(amount)
}
