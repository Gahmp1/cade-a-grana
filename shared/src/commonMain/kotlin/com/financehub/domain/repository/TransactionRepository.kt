package com.financehub.domain.repository

import com.financehub.domain.model.Transaction
import com.financehub.domain.model.MonthReport
import com.financehub.domain.model.ConsolidatedReport

/**
 * Repository para operações com transações
 */
interface TransactionRepository {
    /**
     * Carrega transações de arquivos em um diretório
     */
    suspend fun loadTransactionsFromDirectory(directoryPath: String): List<Transaction>
    
    /**
     * Carrega transações de um arquivo específico
     */
    suspend fun loadTransactionsFromFile(filePath: String): List<Transaction>
    
    /**
     * Gera relatório mensal
     */
    suspend fun generateMonthReport(yearMonth: String, transactions: List<Transaction>): MonthReport
    
    /**
     * Gera relatório consolidado
     */
    suspend fun generateConsolidatedReport(transactions: List<Transaction>): ConsolidatedReport
    
    /**
     * Salva relatório em arquivo
     */
    suspend fun saveReport(report: String, filePath: String)
    
    /**
     * Salva log de mudanças em formato JSON
     */
    suspend fun saveChangeLog(changeLog: String, filePath: String): Result<Unit>
    
    /**
     * Salva transações para um mês específico
     */
    suspend fun saveTransactionsForMonth(yearMonth: String, transactions: List<Transaction>): Result<Unit>
    
    /**
     * Carrega todos os meses salvos
     */
    suspend fun loadAllSavedMonths(): Result<List<String>>
    
    /**
     * Carrega transações de um mês específico
     */
    suspend fun loadTransactionsForMonth(yearMonth: String): Result<List<Transaction>>
}
