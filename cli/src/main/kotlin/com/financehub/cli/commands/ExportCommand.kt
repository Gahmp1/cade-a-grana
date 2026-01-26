package com.financehub.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Export transactions to various formats (JSON, CSV)
 */
class ExportCommand : CliktCommand(
    name = "export",
    help = "Export transactions to JSON or CSV format"
) {
    private val files by argument(help = "Bank statement files to export")
        .file(mustExist = true, canBeDir = false)
        .multiple(required = true)
    
    private val output by option("-o", "--output", help = "Output file")
        .file()
        .required()
    
    private val format by option("-f", "--format", help = "Export format")
        .choice("json", "csv")
        .default("json")
    
    private val categories by option("-c", "--category", help = "Filter by category (can be used multiple times)")
        .multiple()
    
    private val from by option("--from", help = "Start date (DD/MM/YYYY)")
    
    private val to by option("--to", help = "End date (DD/MM/YYYY)")
    
    private val includeMetadata by option("--metadata", help = "Include export metadata in JSON")
        .choice("true", "false")
        .default("true")

    private val json = Json { prettyPrint = true }

    override fun run() = runBlocking {
        val allTransactions = mutableListOf<TransactionData>()
        
        // Parse all files
        files.forEach { file ->
            try {
                val transactions = parseFileForExport(file)
                allTransactions.addAll(transactions)
                echo("📄 Parsed ${transactions.size} transactions from ${file.name}")
            } catch (e: Exception) {
                echo((red)("❌ Error parsing ${file.name}: ${e.message}"), err = true)
            }
        }
        
        if (allTransactions.isEmpty()) {
            echo((yellow)("⚠️  No transactions found"))
            return@runBlocking
        }
        
        // Apply filters
        var filtered = allTransactions.toList()
        
        // Filter by category
        if (categories.isNotEmpty()) {
            filtered = filtered.filter { tx -> 
                categories.any { it.equals(tx.category, ignoreCase = true) }
            }
            echo("🔍 Filtered by categories: ${categories.joinToString(", ")}")
        }
        
        // Filter by date range
        if (from != null || to != null) {
            filtered = filtered.filter { tx ->
                val txDate = parseDate(tx.date)
                val fromDate = from?.let { parseDate(it) }
                val toDate = to?.let { parseDate(it) }
                
                (fromDate == null || txDate >= fromDate) && (toDate == null || txDate <= toDate)
            }
            echo("🔍 Filtered by date range: ${from ?: "start"} to ${to ?: "end"}")
        }
        
        if (filtered.isEmpty()) {
            echo((yellow)("⚠️  No transactions match the specified filters"))
            return@runBlocking
        }
        
        // Sort by date
        filtered = filtered.sortedBy { parseDate(it.date) }
        
        // Export
        val outputText = when (format) {
            "json" -> exportToJson(filtered)
            "csv" -> exportToCsv(filtered)
            else -> exportToJson(filtered)
        }
        
        output.writeText(outputText)
        
        echo((green)("✅ Exported ${filtered.size} transactions to ${output.name}"))
        
        // Summary
        val totalIncome = filtered.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpense = filtered.filter { it.amount < 0 }.sumOf { it.amount }
        echo("")
        echo("Export Summary:")
        echo("  Transactions: ${filtered.size}")
        echo("  " + (green)("Income:  R$ ${"%.2f".format(totalIncome)}"))
        echo("  " + (red)("Expense: R$ ${"%.2f".format(totalExpense)}"))
    }
    
    private fun parseFileForExport(file: File): List<TransactionData> {
        val content = file.readText()
        val fileName = file.name.lowercase()
        
        return when {
            fileName.contains("itau") && fileName.contains("fatura") && fileName.endsWith(".csv") -> {
                content.lines()
                    .drop(1)
                    .filter { it.isNotBlank() && it.contains(",") }
                    .mapNotNull { line ->
                        try {
                            val parts = line.split(",")
                            if (parts.size >= 3) {
                                TransactionData(
                                    date = parts[0],
                                    description = parts[1],
                                    amount = -(parts[2].toDoubleOrNull() ?: 0.0),
                                    category = categorize(parts[1]),
                                    source = "ITAU_CREDIT_CARD"
                                )
                            } else null
                        } catch (e: Exception) { null }
                    }
            }
            else -> emptyList()
        }
    }
    
    private fun categorize(description: String): String {
        val lower = description.lowercase()
        return when {
            lower.containsAny("restaurante", "ifood", "uber eats", "mercado", "padaria") -> "ALIMENTACAO"
            lower.containsAny("uber", "99", "posto", "combustivel", "estacionamento") -> "TRANSPORTE"
            lower.containsAny("aluguel", "condominio", "luz", "agua", "gas") -> "MORADIA"
            lower.containsAny("farmacia", "hospital", "medico", "drogaria") -> "SAUDE"
            lower.containsAny("netflix", "spotify", "cinema", "teatro", "bar") -> "LAZER"
            lower.containsAny("curso", "livro", "escola", "faculdade") -> "EDUCACAO"
            lower.containsAny("roupa", "calcado", "renner", "zara") -> "VESTUARIO"
            lower.containsAny("apple", "samsung", "kabum", "amazon") -> "ELETRONICOS"
            lower.containsAny("assinatura", "mensalidade", "taxa") -> "SERVICOS"
            else -> "OUTROS"
        }
    }
    
    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
    
    private fun parseDate(dateStr: String): Long {
        // DD/MM/YYYY -> YYYYMMDD for sorting
        val parts = dateStr.split("/")
        return if (parts.size == 3) {
            "${parts[2]}${parts[1].padStart(2, '0')}${parts[0].padStart(2, '0')}".toLongOrNull() ?: 0L
        } else 0L
    }
    
    private fun exportToJson(transactions: List<TransactionData>): String {
        return if (includeMetadata == "true") {
            val export = ExportData(
                metadata = ExportMetadata(
                    exportedAt = java.time.Instant.now().toString(),
                    totalTransactions = transactions.size,
                    totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount },
                    totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount },
                    filters = ExportFilters(
                        categories = categories.ifEmpty { null },
                        fromDate = from,
                        toDate = to
                    )
                ),
                transactions = transactions
            )
            json.encodeToString(export)
        } else {
            json.encodeToString(transactions)
        }
    }
    
    private fun exportToCsv(transactions: List<TransactionData>): String {
        val sb = StringBuilder()
        sb.appendLine("date,description,amount,category,source")
        transactions.forEach { tx ->
            val escapedDesc = tx.description.replace("\"", "\"\"")
            sb.appendLine("${tx.date},\"$escapedDesc\",${tx.amount},${tx.category},${tx.source}")
        }
        return sb.toString()
    }
}

@Serializable
data class ExportData(
    val metadata: ExportMetadata,
    val transactions: List<TransactionData>
)

@Serializable
data class ExportMetadata(
    val exportedAt: String,
    val totalTransactions: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val filters: ExportFilters
)

@Serializable
data class ExportFilters(
    val categories: List<String>?,
    val fromDate: String?,
    val toDate: String?
)
