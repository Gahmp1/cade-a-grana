package com.financehub.cli.commands

import com.financehub.data.parser.FileReader
import com.financehub.data.repository.TransactionRepositoryImpl
import com.financehub.domain.model.Transaction
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
 * 
 * Uses the shared module parsers for automatic bank detection:
 * - Itaú (CSV, TXT, PDF)
 * - Nubank (CSV, PDF)
 * - Banco do Brasil (CSV, PDF)
 * - Bradesco (CSV, PDF)
 * - Santander (CSV, PDF)
 * - Caixa (CSV, PDF)
 * - Mercado Pago (CSV)
 * - PicPay (PDF)
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
    
    // Use the shared repository with all parsers
    private val repository = TransactionRepositoryImpl(FileReader())

    override fun run() = runBlocking {
        val allTransactions = mutableListOf<Transaction>()
        
        files.forEachIndexed { index, file ->
            if (verbose) {
                echo((bold + white)("[${index + 1}/${files.size}] ") + "Parsing ${file.name}...")
            }
            
            try {
                val transactions = repository.loadTransactionsFromFile(file.absolutePath)
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
        val sortedTransactions = allTransactions.sortedBy { it.date }
        
        val outputText = when (format) {
            "json" -> formatAsJson(sortedTransactions)
            "csv" -> formatAsCsv(sortedTransactions)
            else -> formatAsTable(sortedTransactions)
        }
        
        if (output != null) {
            output!!.writeText(outputText)
            echo((green)("✅ Saved ${sortedTransactions.size} transactions to ${output!!.name}"))
        } else {
            echo(outputText)
        }
        
        // Summary
        val totalIncome = sortedTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpense = sortedTransactions.filter { it.amount < 0 }.sumOf { it.amount }
        
        echo("")
        echo((bold)("Summary:"))
        echo("  Total transactions: ${sortedTransactions.size}")
        echo("  " + (green)("Income:  R$ ${"%.2f".format(totalIncome)}"))
        echo("  " + (red)("Expense: R$ ${"%.2f".format(totalExpense)}"))
        echo("  Balance: R$ ${"%.2f".format(totalIncome + totalExpense)}")
    }
    
    private fun formatAsTable(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.appendLine("┌────────────┬────────────────────────────────┬──────────────┬──────────────┐")
        sb.appendLine("│ Date       │ Description                    │ Amount       │ Category     │")
        sb.appendLine("├────────────┼────────────────────────────────┼──────────────┼──────────────┤")
        
        transactions.forEach { tx ->
            val desc = tx.description.take(30).padEnd(30)
            val amount = "R$ ${"%.2f".format(tx.amount)}".padStart(12)
            val category = tx.category.name.take(12).padEnd(12)
            val dateStr = tx.date.toString()
            sb.appendLine("│ ${dateStr.padEnd(10)} │ $desc │ $amount │ $category │")
        }
        
        sb.appendLine("└────────────┴────────────────────────────────┴──────────────┴──────────────┘")
        return sb.toString()
    }
    
    private fun formatAsJson(transactions: List<Transaction>): String {
        val data = transactions.map { tx ->
            TransactionData(
                date = tx.date.toString(),
                description = tx.description,
                amount = tx.amount,
                category = tx.category.name,
                source = tx.source.name
            )
        }
        return json.encodeToString(data)
    }
    
    private fun formatAsCsv(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.appendLine("date,description,amount,category,source")
        transactions.forEach { tx ->
            sb.appendLine("${tx.date},\"${tx.description}\",${tx.amount},${tx.category.name},${tx.source.name}")
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
