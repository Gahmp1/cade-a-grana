package com.financehub.domain.usecase

import com.financehub.domain.model.IgnorePatterns
import com.financehub.domain.model.Transaction
import com.financehub.domain.repository.TransactionRepository

/**
 * Use case para preview de transações antes de processar
 */
class PreviewTransactionsUseCase(
    private val repository: TransactionRepository
) {
    /**
     * Faz preview de transações de arquivos selecionados
     * @param filePaths Lista de caminhos de arquivos para preview
     * @param ignorePatterns Padrões para auto-ignorar transações
     * @return Result com lista de transações ou erro
     */
    suspend operator fun invoke(filePaths: List<String>, ignorePatterns: IgnorePatterns = IgnorePatterns.default()): Result<List<Transaction>> {
        return try {
            println("🔍 PreviewTransactionsUseCase: Iniciando preview de ${filePaths.size} arquivo(s)")
            
            if (filePaths.isEmpty()) {
                println("❌ PreviewTransactionsUseCase: Nenhum arquivo selecionado")
                return Result.failure(IllegalArgumentException("Nenhum arquivo selecionado"))
            }
            
            val allTransactions = mutableListOf<Transaction>()
            val errors = mutableListOf<String>()
            
            filePaths.forEachIndexed { index, filePath ->
                try {
                    val fileName = filePath.split("/").last()
                    println("📄 Processando arquivo ${index + 1}/${filePaths.size}: $fileName")
                    
                    val transactions = repository.loadTransactionsFromFile(filePath)
                    println("   └─ Encontradas ${transactions.size} transações")
                    
                    if (transactions.isNotEmpty()) {
                        allTransactions.addAll(transactions)
                    } else {
                        val error = "$fileName: nenhuma transação encontrada"
                        println("   ⚠️ $error")
                        errors.add(error)
                    }
                } catch (e: Exception) {
                    val fileName = filePath.split("/").last()
                    val error = "$fileName: ${e.message}"
                    println("   ❌ Erro: ${e.message}")
                    e.printStackTrace()
                    errors.add(error)
                }
            }
            
            // Aplicar padrões de ignorar automaticamente
            val transactionsWithIgnorePatterns = allTransactions.map { transaction ->
                // Verificar padrão simples
                val matchesSimple = ignorePatterns.matchesSimplePattern(transaction.description)
                
                // Verificar regra avançada
                val matchedAdvancedRule = ignorePatterns.matchesAdvancedRule(
                    transaction.description, 
                    transaction.amount
                )
                
                when {
                    matchedAdvancedRule != null -> {
                        // Regra avançada: ignorar + marcar para revisão
                        transaction.copy(isIgnored = true, needsReview = true)
                    }
                    matchesSimple -> {
                        // Padrão simples: apenas ignorar
                        transaction.copy(isIgnored = true)
                    }
                    else -> transaction
                }
            }
            
            val ignoredCount = transactionsWithIgnorePatterns.count { it.isIgnored }
            val reviewCount = transactionsWithIgnorePatterns.count { it.needsReview }
            
            if (ignoredCount > 0) {
                println("   🔇 Auto-ignoradas por padrão: $ignoredCount")
            }
            if (reviewCount > 0) {
                println("   ⚠️  Necessitam revisão (regra avançada): $reviewCount")
            }
            
            println("\n📊 Resultado do preview:")
            println("   ✓ Total de transações: ${allTransactions.size}")
            println("   ⚠ Erros/avisos: ${errors.size}")
            
            if (allTransactions.isEmpty() && errors.isNotEmpty()) {
                val errorMsg = "Falha ao processar arquivos:\n${errors.joinToString("\n")}"
                println("❌ $errorMsg")
                Result.failure(Exception(errorMsg))
            } else if (errors.isNotEmpty()) {
                println("⚠️ Avisos de parsing (mas ${allTransactions.size} transações foram encontradas):")
                errors.forEach { println("   - $it") }
                Result.success(transactionsWithIgnorePatterns)
            } else {
                println("✅ Preview concluído com sucesso!")
                Result.success(transactionsWithIgnorePatterns)
            }
        } catch (e: Exception) {
            println("❌ Erro fatal no PreviewTransactionsUseCase: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
