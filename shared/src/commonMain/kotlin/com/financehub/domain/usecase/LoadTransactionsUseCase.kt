package com.financehub.domain.usecase

import com.financehub.domain.model.Transaction
import com.financehub.domain.repository.TransactionRepository

/**
 * Use case para carregar transações de um diretório
 */
class LoadTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(directoryPath: String): Result<List<Transaction>> {
        return try {
            val transactions = repository.loadTransactionsFromDirectory(directoryPath)
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
