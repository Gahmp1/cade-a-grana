package com.financehub.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Generate financial reports from bank statement files
 */
class ReportCommand : CliktCommand(
    name = "report",
    help = "Generate financial reports from parsed transactions"
) {
    private val files by argument(help = "Bank statement files to analyze")
        .file(mustExist = true, canBeDir = false)
        .multiple(required = true)
    
    private val month by option("-m", "--month", help = "Filter by month (YYYY-MM format)")
    
    private val output by option("-o", "--output", help = "Output file (prints to stdout if not specified)")
        .file()
    
    private val outputFormat by option("-f", "--format", help = "Output format")
        .choice("text", "markdown")
        .default("text")

    override fun run() = runBlocking {
        val allTransactions = mutableListOf<TransactionData>()
        
        // Parse all files
        files.forEach { file ->
            try {
                val transactions = parseFileForReport(file)
                allTransactions.addAll(transactions)
            } catch (e: Exception) {
                echo((red)("❌ Error parsing ${file.name}: ${e.message}"), err = true)
            }
        }
        
        if (allTransactions.isEmpty()) {
            echo((yellow)("⚠️  No transactions found"))
            return@runBlocking
        }
        
        // Filter by month if specified
        val filtered = if (month != null) {
            allTransactions.filter { tx ->
                // Assuming date format DD/MM/YYYY
                val parts = tx.date.split("/")
                if (parts.size >= 3) {
                    val txMonth = "${parts[2]}-${parts[1].padStart(2, '0')}"
                    txMonth == month
                } else false
            }
        } else {
            allTransactions
        }
        
        if (filtered.isEmpty()) {
            echo((yellow)("⚠️  No transactions found for the specified period"))
            return@runBlocking
        }
        
        val report = generateReport(filtered, month)
        
        if (output != null) {
            output!!.writeText(report)
            echo((green)("✅ Report saved to ${output!!.name}"))
        } else {
            echo(report)
        }
    }
    
    private fun parseFileForReport(file: File): List<TransactionData> {
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
    
    private fun generateReport(transactions: List<TransactionData>, period: String?): String {
        val sb = StringBuilder()
        val periodLabel = period ?: "All Time"
        
        if (outputFormat == "markdown") {
            sb.appendLine("# Financial Report - $periodLabel")
            sb.appendLine()
        } else {
            sb.appendLine("═══════════════════════════════════════════════════")
            sb.appendLine("           FINANCIAL REPORT - $periodLabel")
            sb.appendLine("═══════════════════════════════════════════════════")
            sb.appendLine()
        }
        
        // Group by category
        val byCategory = transactions.groupBy { it.category }
        
        val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount }
        val balance = totalIncome + totalExpense
        
        // Summary
        if (outputFormat == "markdown") {
            sb.appendLine("## Summary")
            sb.appendLine()
            sb.appendLine("| Metric | Value |")
            sb.appendLine("|--------|-------|")
            sb.appendLine("| Total Transactions | ${transactions.size} |")
            sb.appendLine("| Total Income | R$ ${"%.2f".format(totalIncome)} |")
            sb.appendLine("| Total Expenses | R$ ${"%.2f".format(totalExpense)} |")
            sb.appendLine("| **Balance** | **R$ ${"%.2f".format(balance)}** |")
            sb.appendLine()
        } else {
            sb.appendLine("SUMMARY")
            sb.appendLine("───────────────────────────────────────────────────")
            sb.appendLine("  Total Transactions: ${transactions.size}")
            sb.appendLine("  Total Income:       R$ ${"%.2f".format(totalIncome)}")
            sb.appendLine("  Total Expenses:     R$ ${"%.2f".format(totalExpense)}")
            sb.appendLine("  Balance:            R$ ${"%.2f".format(balance)}")
            sb.appendLine()
        }
        
        // By category
        if (outputFormat == "markdown") {
            sb.appendLine("## Expenses by Category")
            sb.appendLine()
            sb.appendLine("| Category | Amount | % |")
            sb.appendLine("|----------|--------|---|")
        } else {
            sb.appendLine("EXPENSES BY CATEGORY")
            sb.appendLine("───────────────────────────────────────────────────")
        }
        
        val expenseCategories = byCategory
            .mapValues { (_, txs) -> txs.filter { it.amount < 0 }.sumOf { it.amount } }
            .filter { it.value < 0 }
            .toList()
            .sortedBy { it.second }
        
        expenseCategories.forEach { (category, amount) ->
            val percentage = if (totalExpense != 0.0) (amount / totalExpense * 100) else 0.0
            
            if (outputFormat == "markdown") {
                sb.appendLine("| $category | R$ ${"%.2f".format(amount)} | ${"%.1f".format(percentage)}% |")
            } else {
                val bar = "█".repeat((percentage / 5).toInt().coerceIn(0, 20))
                sb.appendLine("  ${category.padEnd(15)} R$ ${"%.2f".format(amount).padStart(10)}  ${"%.1f".format(percentage).padStart(5)}% $bar")
            }
        }
        
        sb.appendLine()
        
        // Top expenses
        if (outputFormat == "markdown") {
            sb.appendLine("## Top 10 Expenses")
            sb.appendLine()
            sb.appendLine("| Date | Description | Amount |")
            sb.appendLine("|------|-------------|--------|")
        } else {
            sb.appendLine("TOP 10 EXPENSES")
            sb.appendLine("───────────────────────────────────────────────────")
        }
        
        transactions
            .filter { it.amount < 0 }
            .sortedBy { it.amount }
            .take(10)
            .forEach { tx ->
                if (outputFormat == "markdown") {
                    sb.appendLine("| ${tx.date} | ${tx.description} | R$ ${"%.2f".format(tx.amount)} |")
                } else {
                    sb.appendLine("  ${tx.date}  ${tx.description.take(25).padEnd(25)}  R$ ${"%.2f".format(tx.amount)}")
                }
            }
        
        if (outputFormat != "markdown") {
            sb.appendLine()
            sb.appendLine("═══════════════════════════════════════════════════")
        }
        
        return sb.toString()
    }
}
