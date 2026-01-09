package com.financehub.domain.usecase

import com.financehub.domain.model.ConsolidatedReport
import com.financehub.domain.model.Transaction
import com.financehub.domain.repository.TransactionRepository

/**
 * Use case para gerar relatório consolidado
 */
class GenerateConsolidatedReportUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactions: List<Transaction>): Result<ConsolidatedReport> {
        return try {
            val report = repository.generateConsolidatedReport(transactions)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
