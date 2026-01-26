package com.financehub.cli.commands

import com.financehub.data.parser.*
import com.financehub.data.parser.common.BaseParser
import com.financehub.domain.model.Transaction
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Bancos suportados pelo FinanceHub
 */
enum class Bank(val displayName: String) {
    ITAU("Itaú"),
    NUBANK("Nubank"),
    BRADESCO("Bradesco"),
    SANTANDER("Santander"),
    CAIXA("Caixa Econômica"),
    BB("Banco do Brasil"),
    MERCADOPAGO("Mercado Pago"),
    PICPAY("PicPay");
    
    companion object {
        fun fromString(value: String): Bank? = entries.find { 
            it.name.equals(value, ignoreCase = true) 
        }
    }
}

/**
 * Tipo de documento financeiro
 */
enum class DocumentType(val displayName: String) {
    EXTRATO("Extrato Bancário"),
    FATURA("Fatura de Cartão");
    
    companion object {
        fun fromString(value: String): DocumentType? = entries.find { 
            it.name.equals(value, ignoreCase = true) 
        }
    }
}

/**
 * Parse bank statement files and display transactions
 * 
 * Supported banks and document types:
 * - Itaú: extrato (CSV, TXT, PDF), fatura (CSV)
 * - Nubank: extrato (PDF), fatura (CSV)
 * - Bradesco: extrato (CSV, PDF)
 * - Santander: extrato (CSV, PDF)
 * - Caixa: extrato (CSV, PDF)
 * - Banco do Brasil: extrato (CSV, PDF)
 * - Mercado Pago: extrato (CSV)
 * - PicPay: extrato (PDF)
 * 
 * 💡 Dica: Prefira usar arquivos CSV sempre que possível.
 *    São mais simples de processar e menos propensos a erros.
 */
class ParseCommand : CliktCommand(
    name = "parse",
    help = """
        Parse bank statement files (CSV, TXT, PDF) and display transactions.
        
        You must specify the bank and document type.
        
        💡 TIP: Prefer CSV files when available - they are simpler and more reliable.
        
        Examples:
          fhub parse -b itau -t fatura fatura.csv
          fhub parse -b nubank -t fatura nubank.csv
          fhub parse -b itau -t extrato extrato.txt
          fhub parse -b bb -t extrato extrato.csv
    """.trimIndent()
) {
    private val banco by option("-b", "--banco", help = "Bank name (required)")
        .choice(
            "itau" to Bank.ITAU,
            "nubank" to Bank.NUBANK,
            "bradesco" to Bank.BRADESCO,
            "santander" to Bank.SANTANDER,
            "caixa" to Bank.CAIXA,
            "bb" to Bank.BB,
            "mercadopago" to Bank.MERCADOPAGO,
            "picpay" to Bank.PICPAY,
            ignoreCase = true
        )
        .required()
    
    private val tipo by option("-t", "--tipo", help = "Document type (required)")
        .choice(
            "extrato" to DocumentType.EXTRATO,
            "fatura" to DocumentType.FATURA,
            ignoreCase = true
        )
        .required()
    
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
    
    /**
     * Retorna o parser correto baseado no banco e tipo de documento
     */
    private fun getParser(bank: Bank, docType: DocumentType, fileExtension: String): FileParser? {
        return when (bank) {
            Bank.ITAU -> when (docType) {
                DocumentType.FATURA -> ItauCreditCardParser()
                DocumentType.EXTRATO -> when (fileExtension.lowercase()) {
                    "pdf" -> ItauPdfParser()
                    else -> ItauBankStatementParser()
                }
            }
            Bank.NUBANK -> when (docType) {
                DocumentType.FATURA -> NubankCsvParser()
                DocumentType.EXTRATO -> NubankPdfParser()
            }
            Bank.BRADESCO -> when (docType) {
                DocumentType.EXTRATO -> when (fileExtension.lowercase()) {
                    "pdf" -> BradescoPdfParser()
                    else -> BradescoCsvParser()
                }
                DocumentType.FATURA -> {
                    echo((yellow)("⚠️  Fatura Bradesco ainda não suportada. Use extrato."))
                    null
                }
            }
            Bank.SANTANDER -> when (docType) {
                DocumentType.EXTRATO -> when (fileExtension.lowercase()) {
                    "pdf" -> SantanderPdfParser()
                    else -> SantanderCsvParser()
                }
                DocumentType.FATURA -> {
                    echo((yellow)("⚠️  Fatura Santander ainda não suportada. Use extrato."))
                    null
                }
            }
            Bank.CAIXA -> when (docType) {
                DocumentType.EXTRATO -> when (fileExtension.lowercase()) {
                    "pdf" -> CaixaPdfParser()
                    else -> CaixaCsvParser()
                }
                DocumentType.FATURA -> {
                    echo((yellow)("⚠️  Fatura Caixa ainda não suportada. Use extrato."))
                    null
                }
            }
            Bank.BB -> when (docType) {
                DocumentType.EXTRATO -> when (fileExtension.lowercase()) {
                    "pdf" -> BancoBrasilPdfParser()
                    else -> BancoBrasilCsvParser()
                }
                DocumentType.FATURA -> {
                    echo((yellow)("⚠️  Fatura Banco do Brasil ainda não suportada. Use extrato."))
                    null
                }
            }
            Bank.MERCADOPAGO -> when (docType) {
                DocumentType.EXTRATO -> MercadoPagoCsvParser()
                DocumentType.FATURA -> {
                    echo((yellow)("⚠️  Mercado Pago não possui fatura. Use extrato."))
                    null
                }
            }
            Bank.PICPAY -> when (docType) {
                DocumentType.EXTRATO -> PicPayStatementParser()
                DocumentType.FATURA -> {
                    echo((yellow)("⚠️  PicPay não possui fatura. Use extrato."))
                    null
                }
            }
        }
    }

    override fun run() = runBlocking {
        val allTransactions = mutableListOf<Transaction>()
        
        // Mostrar dica sobre CSV se estiver usando PDF
        val hasPdf = files.any { it.extension.lowercase() == "pdf" }
        if (hasPdf) {
            echo("")
            echo((cyan)("💡 Dica: Prefira usar arquivos CSV quando disponível."))
            echo((cyan)("   São mais simples de processar e menos propensos a erros."))
            echo("")
        }
        
        echo((bold)("📊 ${banco.displayName} - ${tipo.displayName}"))
        echo("")
        
        files.forEachIndexed { index, file ->
            val fileExtension = file.extension
            
            if (verbose) {
                echo((bold + white)("[${index + 1}/${files.size}] ") + "Parsing ${file.name}...")
            }
            
            val parser = getParser(banco, tipo, fileExtension)
            if (parser == null) {
                echo((red)("  ❌ No parser available for ${banco.displayName} ${tipo.displayName}"), err = true)
                return@forEachIndexed
            }
            
            try {
                val content = file.readText()
                val transactions = parser.parse(content, file.name)
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
