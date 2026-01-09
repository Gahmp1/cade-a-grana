package com.financehub.data.repository

import com.financehub.data.parser.*
import com.financehub.domain.model.ConsolidatedReport
import com.financehub.domain.model.MonthReport
import com.financehub.domain.model.Transaction
import com.financehub.domain.repository.TransactionRepository
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Implementação do TransactionRepository
 */
class TransactionRepositoryImpl(
    private val fileReader: FileReader
) : TransactionRepository {
    
    private val parsers: List<FileParser> = listOf(
        ItauCreditCardParser(),
        ItauBankStatementParser(),
        PicPayStatementParser()
    )
    
    override suspend fun loadTransactionsFromDirectory(directoryPath: String): List<Transaction> {
        val allTransactions = mutableListOf<Transaction>()
        
        try {
            val files = fileReader.listFiles(directoryPath)
            
            files.forEach { filePath ->
                val fileName = filePath.substringAfterLast("/")
                val transactions = loadTransactionsFromFile(filePath)
                allTransactions.addAll(transactions)
            }
            
        } catch (e: Exception) {
            println("Error loading transactions from directory: ${e.message}")
        }
        
        return allTransactions.sortedBy { it.date }
    }
    
    override suspend fun loadTransactionsFromFile(filePath: String): List<Transaction> {
        val fileName = filePath.substringAfterLast("/")
        
        val parser = parsers.firstOrNull { it.supports(fileName) }
            ?: return emptyList()
        
        return try {
            val content = fileReader.readFile(filePath)
            parser.parse(content, fileName)
        } catch (e: Exception) {
            println("Error parsing file $fileName: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun generateMonthReport(
        yearMonth: String,
        transactions: List<Transaction>
    ): MonthReport {
        val monthTransactions = transactions.filter { 
            "${it.date.year}-${it.date.monthNumber.toString().padStart(2, '0')}" == yearMonth
        }
        
        val dataSources = monthTransactions.map { it.source }.toSet()
        
        return MonthReport(
            yearMonth = yearMonth,
            transactions = monthTransactions,
            dataSources = dataSources
        )
    }
    
    override suspend fun generateConsolidatedReport(
        transactions: List<Transaction>
    ): ConsolidatedReport {
        // Agrupar por mês
        val byMonth = transactions.groupBy { 
            "${it.date.year}-${it.date.monthNumber.toString().padStart(2, '0')}"
        }
        
        val monthReports = byMonth.map { (yearMonth, monthTransactions) ->
            val dataSources = monthTransactions.map { it.source }.toSet()
            MonthReport(
                yearMonth = yearMonth,
                transactions = monthTransactions,
                dataSources = dataSources
            )
        }.sortedBy { it.yearMonth }
        
        val startMonth = monthReports.firstOrNull()?.yearMonth ?: ""
        val endMonth = monthReports.lastOrNull()?.yearMonth ?: ""
        
        return ConsolidatedReport(
            monthReports = monthReports,
            startMonth = startMonth,
            endMonth = endMonth
        )
    }
    
    override suspend fun saveReport(report: String, filePath: String) {
        // Implementação específica por plataforma via expect/actual
        // Por enquanto, apenas log
        println("Saving report to: $filePath")
    }
    
    override suspend fun saveChangeLog(changeLog: String, filePath: String): Result<Unit> {
        return try {
            fileReader.writeFile(filePath, changeLog)
            println("✅ Log de mudanças salvo em: $filePath")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Erro ao salvar log: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun saveTransactionsForMonth(yearMonth: String, transactions: List<Transaction>): Result<Unit> {
        return try {
            val homeDir = System.getProperty("user.home")
            val financeHubDir = File(homeDir, ".financehub/transactions")
            if (!financeHubDir.exists()) {
                financeHubDir.mkdirs()
            }
            
            val file = File(financeHubDir, "$yearMonth.json")
            val json = Json { prettyPrint = true }
            val jsonContent = json.encodeToString<List<Transaction>>(transactions)
            file.writeText(jsonContent)
            
            println("💾 Transações salvas: $yearMonth (${transactions.size} transações)")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Erro ao salvar transações: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun loadAllSavedMonths(): Result<List<String>> {
        return try {
            val homeDir = System.getProperty("user.home")
            val transactionsDir = File(homeDir, ".financehub/transactions")
            
            if (!transactionsDir.exists()) {
                return Result.success(emptyList())
            }
            
            val months = transactionsDir.listFiles()?.filter { it.extension == "json" }
                ?.map { it.nameWithoutExtension }
                ?.sorted()
                ?: emptyList()
            
            println("📂 Meses salvos encontrados: ${months.size}")
            Result.success(months)
        } catch (e: Exception) {
            println("❌ Erro ao carregar meses: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun loadTransactionsForMonth(yearMonth: String): Result<List<Transaction>> {
        return try {
            val homeDir = System.getProperty("user.home")
            val file = File(homeDir, ".financehub/transactions/$yearMonth.json")
            
            if (!file.exists()) {
                return Result.success(emptyList())
            }
            
            val jsonContent = file.readText()
            val json = Json { ignoreUnknownKeys = true }
            val transactions = json.decodeFromString<List<Transaction>>(jsonContent)
            
            println("📖 Carregadas ${transactions.size} transações de $yearMonth")
            Result.success(transactions)
        } catch (e: Exception) {
            println("❌ Erro ao carregar transações de $yearMonth: ${e.message}")
            Result.failure(e)
        }
    }
}
