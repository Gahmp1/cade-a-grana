package com.financehub.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Parse bank statement files and display transactions
 */
class ParseCommand : CliktCommand(
    name = "parse",
    help = "Parse bank statement files (CSV, TXT, PDF) and display transactions"
) {
    private val files by argument(help = "Bank statement files to parse")
        .file(mustExist = true, canBeDir = false)
        .multiple(required = true)
    
    private val format by option("-f", "--format", help = "Output format")
        .choice("table", "json", "csv")
        .default("table")
    
    private val output by option("-o", "--output", help = "Output file (prints to stdout if not specified)")
        .file()
    
    private val verbose by option("-v", "--verbose", help = "Show detailed parsing info")
        .flag(default = false)

    private val json = Json { prettyPrint = true }

    override fun run() = runBlocking {
        val allTransactions = mutableListOf<TransactionData>()
        
        files.forEachIndexed { index, file ->
            if (verbose) {
                echo((bold + white)("[${index + 1}/${files.size}] ") + "Parsing ${file.name}...")
            }
            
            try {
                val transactions = parseFile(file)
                allTransactions.addAll(transactions)
                
                if (verbose) {
                    echo((green)("  ✅ Found ${transactions.size} transactions"))
                }
            } catch (e: Exception) {
                echo((red)("  ❌ Error parsing ${file.name}: ${e.message}"), err = true)
            }
        }
        
        if (allTransactions.isEmpty()) {
            echo((yellow)("⚠️  No transactions found"))
            return@runBlocking
        }
        
        // Sort by date
        allTransactions.sortBy { it.date }
        
        val outputText = when (format) {
            "json" -> formatAsJson(allTransactions)
            "csv" -> formatAsCsv(allTransactions)
            else -> formatAsTable(allTransactions)
        }
        
        if (output != null) {
            output!!.writeText(outputText)
            echo((green)("✅ Saved ${allTransactions.size} transactions to ${output!!.name}"))
        } else {
            echo(outputText)
        }
        
        // Summary
        val totalIncome = allTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpense = allTransactions.filter { it.amount < 0 }.sumOf { it.amount }
        
        echo("")
        echo((bold)("Summary:"))
        echo("  Total transactions: ${allTransactions.size}")
        echo("  " + (green)("Income:  R$ ${"%.2f".format(totalIncome)}"))
        echo("  " + (red)("Expense: R$ ${"%.2f".format(totalExpense)}"))
        echo("  Balance: R$ ${"%.2f".format(totalIncome + totalExpense)}")
    }
    
    private suspend fun parseFile(file: File): List<TransactionData> {
        val content = file.readText()
        val fileName = file.name.lowercase()
        
        // Detect parser based on filename
        return when {
            fileName.contains("itau") && fileName.contains("fatura") && fileName.endsWith(".csv") -> {
                parseItauCreditCard(content, file.name)
            }
            fileName.contains("itau") && fileName.contains("extrato") && fileName.endsWith(".txt") -> {
                parseItauBankStatement(content, file.name)
            }
            else -> {
                throw UnsupportedOperationException("Unsupported file format: ${file.name}")
            }
        }
    }
    
    private fun parseItauCreditCard(content: String, fileName: String): List<TransactionData> {
        return content.lines()
            .drop(1) // Skip header
            .filter { it.isNotBlank() && it.contains(",") }
            .mapNotNull { line ->
                try {
                    val parts = line.split(",")
                    if (parts.size >= 3) {
                        val date = parts[0]
                        val description = parts[1]
                        val rawAmount = parts[2].toDoubleOrNull() ?: return@mapNotNull null
                        val amount = -rawAmount // Convert positive expenses to negative
                        
                        TransactionData(
                            date = date,
                            description = description,
                            amount = amount,
                            category = categorize(description),
                            source = "ITAU_CREDIT_CARD"
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    private fun parseItauBankStatement(content: String, fileName: String): List<TransactionData> {
        // TODO: Implement Itau bank statement parser
        return emptyList()
    }
    
    private fun categorize(description: String): String {
        val lower = description.lowercase()
        return when {
            lower.containsAny("restaurante", "ifood", "uber eats", "mercado", "padaria", "lanchonete") -> "ALIMENTACAO"
            lower.containsAny("uber", "99", "posto", "combustivel", "estacionamento", "pedagio") -> "TRANSPORTE"
            lower.containsAny("aluguel", "condominio", "luz", "agua", "gas", "iptu") -> "MORADIA"
            lower.containsAny("farmacia", "hospital", "medico", "drogaria", "clinica", "saude") -> "SAUDE"
            lower.containsAny("netflix", "spotify", "cinema", "teatro", "bar", "show") -> "LAZER"
            lower.containsAny("curso", "livro", "escola", "faculdade", "udemy", "alura") -> "EDUCACAO"
            lower.containsAny("roupa", "calcado", "renner", "zara", "nike", "adidas") -> "VESTUARIO"
            lower.containsAny("apple", "samsung", "kabum", "amazon", "mercadolivre") -> "ELETRONICOS"
            lower.containsAny("assinatura", "mensalidade", "taxa", "tarifa") -> "SERVICOS"
            else -> "OUTROS"
        }
    }
    
    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
    
    private fun formatAsTable(transactions: List<TransactionData>): String {
        val sb = StringBuilder()
        sb.appendLine("┌────────────┬────────────────────────────────┬──────────────┬──────────────┐")
        sb.appendLine("│ Date       │ Description                    │ Amount       │ Category     │")
        sb.appendLine("├────────────┼────────────────────────────────┼──────────────┼──────────────┤")
        
        transactions.forEach { tx ->
            val desc = tx.description.take(30).padEnd(30)
            val amount = "R$ ${"%.2f".format(tx.amount)}".padStart(12)
            val category = tx.category.take(12).padEnd(12)
            sb.appendLine("│ ${tx.date.padEnd(10)} │ $desc │ $amount │ $category │")
        }
        
        sb.appendLine("└────────────┴────────────────────────────────┴──────────────┴──────────────┘")
        return sb.toString()
    }
    
    private fun formatAsJson(transactions: List<TransactionData>): String {
        return json.encodeToString(transactions)
    }
    
    private fun formatAsCsv(transactions: List<TransactionData>): String {
        val sb = StringBuilder()
        sb.appendLine("date,description,amount,category,source")
        transactions.forEach { tx ->
            sb.appendLine("${tx.date},\"${tx.description}\",${tx.amount},${tx.category},${tx.source}")
        }
        return sb.toString()
    }
}

@kotlinx.serialization.Serializable
data class TransactionData(
    val date: String,
    val description: String,
    val amount: Double,
    val category: String,
    val source: String
)
