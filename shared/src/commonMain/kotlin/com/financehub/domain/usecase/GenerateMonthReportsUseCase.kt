package com.financehub.domain.usecase

import com.financehub.domain.model.MonthReport
import com.financehub.domain.model.Transaction
import com.financehub.domain.repository.TransactionRepository

/**
 * Use case para gerar relatórios mensais
 */
class GenerateMonthReportsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transactions: List<Transaction>,
        maxReports: Int = 300
    ): Result<List<MonthReport>> {
        return try {
            // Agrupar por mês
            val byMonth = transactions.groupBy { 
                "${it.date.year}-${it.date.monthNumber.toString().padStart(2, '0')}"
            }
            
            // Criar relatórios
            val allReports = byMonth.map { (yearMonth, monthTransactions) ->
                repository.generateMonthReport(yearMonth, transactions)
            }
            
            // Ordenar por data decrescente (meses mais recentes primeiro)
            val sortedReports = allReports.sortedByDescending { it.yearMonth }
            
            val selectedReports = sortedReports.take(maxReports)
            
            Result.success(selectedReports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
