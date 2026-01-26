package com.financehub.data.repository

import com.financehub.data.parser.*
import com.financehub.data.parser.common.BaseParser
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
 * 
 * Usa sistema de detecção automática de parsers baseado em:
 * 1. Extensão do arquivo
 * 2. Nome do arquivo (keywords)
 * 3. Conteúdo do arquivo (detectConfidence)
 */
class TransactionRepositoryImpl(
    private val fileReader: FileReader
) : TransactionRepository {
    
    /**
     * Lista de parsers disponíveis.
     * Ordem importa: parsers mais específicos primeiro.
     */
    private val parsers: List<FileParser> = listOf(
        // Itaú Parsers (CSV, TXT, PDF)
        ItauCreditCardParser(),
        ItauBankStatementParser(),
        ItauPdfParser(),
        // Nubank Parsers (CSV, PDF)
        NubankCsvParser(),
        NubankPdfParser(),
        // Mercado Pago (CSV)
        MercadoPagoCsvParser(),
        // Bradesco (CSV, PDF)
        BradescoCsvParser(),
        BradescoPdfParser(),
        // Santander (CSV, PDF)
        SantanderCsvParser(),
        SantanderPdfParser(),
        // Caixa (CSV, PDF)
        CaixaCsvParser(),
        CaixaPdfParser(),
        // Banco do Brasil (CSV, PDF)
        BancoBrasilCsvParser(),
        BancoBrasilPdfParser(),
        // PicPay (PDF)
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
        
        // Primeiro: tentar por supports() (rápido, baseado em nome)
        var parser = parsers.firstOrNull { it.supports(fileName) }
        
        // Se não encontrou, tentar detecção por conteúdo
        if (parser == null) {
            parser = detectParserByContent(filePath, fileName)
        }
        
        if (parser == null) {
            println("   ⚠️ Nenhum parser encontrado para: $fileName")
            logAvailableParsers(fileName)
            return emptyList()
        }
        
        return try {
            val content = fileReader.readFile(filePath)
            val parserName = (parser as? BaseParser)?.displayName ?: parser::class.simpleName
            println("   🔧 Usando parser: $parserName")
            parser.parse(content, fileName)
        } catch (e: Exception) {
            println("Error parsing file $fileName: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Detecta o melhor parser baseado no conteúdo do arquivo
     */
    private suspend fun detectParserByContent(filePath: String, fileName: String): FileParser? {
        return try {
            // Ler primeiras linhas para análise
            val content = fileReader.readFile(filePath)
            val preview = content.lines().take(20).joinToString("\n")
            
            // Calcular confiança de cada parser
            val parserConfidences = parsers.mapNotNull { parser ->
                if (parser is BaseParser) {
                    val confidence = parser.detectConfidence(fileName, preview)
                    if (confidence > 0.3f) { // Threshold mínimo
                        Pair(parser, confidence)
                    } else null
                } else null
            }
            
            if (parserConfidences.isNotEmpty()) {
                val best = parserConfidences.maxByOrNull { it.second }!!
                println("   🔍 Detecção automática: ${best.first.displayName} (${(best.second * 100).toInt()}% confiança)")
                best.first
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Log para debug: mostra quais parsers foram testados
     */
    private fun logAvailableParsers(fileName: String) {
        println("   📋 Parsers disponíveis:")
        parsers.forEach { parser ->
            val name = (parser as? BaseParser)?.displayName ?: parser::class.simpleName
            val supports = parser.supports(fileName)
            println("      - $name: supports=$supports")
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
