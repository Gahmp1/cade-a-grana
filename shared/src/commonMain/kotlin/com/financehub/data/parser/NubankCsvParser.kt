package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.CsvParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para fatura de cartão de crédito Nubank (CSV).
 * 
 * Formato típico do CSV exportado pelo Nubank:
 * - Header: "date,title,amount" (inglês) ou "data,categoria,titulo,valor" (português)
 * - Data: yyyy-MM-dd (ISO)
 * - Valor: decimal negativo para compras (ex: -29.90)
 * - Delimitador: vírgula
 */
class NubankCsvParser : CsvParser() {
    
    override val bankIdentifier = "nubank"
    override val displayName = "Fatura Nubank"
    override val dataSource = DataSource.NUBANK_CREDIT_CARD
    
    override val delimiter = ","
    override val hasHeader = true
    override val minColumns = 3
    
    // Keywords para detecção automática
    override val contentKeywords = listOf("nubank", "nu bank", "roxinho")
    override val headerPatterns = listOf(
        "date,title,amount",
        "date,category,title,amount"
    )
    
    // Índices dinâmicos baseados no header detectado
    private var detectedDateIndex = 0
    private var detectedDescriptionIndex = 1
    private var detectedAmountIndex = 2
    
    // Flag para indicar se é fatura de cartão (valores positivos = gastos)
    private var isCreditCardStatement = false
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        detectHeaderFormat(fileContent.lines().firstOrNull() ?: "")
        // Detectar se é fatura de cartão pelo nome do arquivo
        isCreditCardStatement = isCreditCardFile(fileName)
        return super.parse(fileContent, fileName)
    }
    
    /**
     * Detecta se o arquivo é uma fatura de cartão de crédito.
     * Faturas têm valores positivos que representam gastos (devem ser negativos).
     */
    private fun isCreditCardFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return lower.contains("fatura") ||
               lower.contains("cartao") ||
               lower.contains("cartão") ||
               lower.contains("credit") ||
               // Padrão Nubank fatura: Nubank_2026-02-03.csv (sem "extrato" ou "statement")
               (lower.contains("nubank") && !lower.contains("extrato") && !lower.contains("statement"))
    }
    
    private fun detectHeaderFormat(headerLine: String) {
        val columns = headerLine.split(delimiter).map { it.trim().lowercase() }
        columns.forEachIndexed { index, col ->
            when {
                col.contains("date") || col.contains("data") -> detectedDateIndex = index
                col.contains("title") || col.contains("titulo") || col.contains("título") -> {
                    detectedDescriptionIndex = index
                }
                col.contains("amount") || col.contains("valor") -> detectedAmountIndex = index
            }
        }
    }
    
    override fun parseRow(
        columns: List<String>,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): Transaction? {
        val dateStr = columns.getOrNull(detectedDateIndex)
            ?.trim()?.replace("\"", "") ?: return null
        val description = columns.getOrNull(detectedDescriptionIndex)
            ?.trim()?.replace("\"", "") ?: return null
        val amountStr = columns.getOrNull(detectedAmountIndex)
            ?.trim()?.replace("\"", "") ?: return null
        
        if (dateStr.isEmpty() || description.isEmpty() || amountStr.isEmpty()) {
            return null
        }
        
        val date = BrazilianPatterns.parseDateAuto(dateStr) ?: return null
        var amount = BrazilianPatterns.parseValueAuto(amountStr) ?: return null
        
        // Se for fatura de cartão, valores positivos são gastos (inverter sinal)
        // Valores negativos (ex: "Pagamento recebido") mantém o sinal
        if (isCreditCardStatement && amount > 0) {
            amount = -amount
        }
        
        return Transaction(
            id = generateIdWithDate(fileName, lineIndex, description, dateStr),
            date = date,
            description = description,
            amount = amount,
            category = Category.classify(description),
            source = dataSource,
            rawData = rawLine
        )
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return lowerName.endsWith(".csv") && 
               (lowerName.contains("nubank") || lowerName.contains("nu_"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.endsWith(".csv")) return 0f
        
        var confidence = 0f
        val firstLine = contentPreview.lines().firstOrNull()?.lowercase() ?: ""
        
        // Header característico do Nubank (inglês)
        if (firstLine.contains("date,") && firstLine.contains("amount")) {
            confidence += 0.6f
        }
        if (firstLine.contains("title")) {
            confidence += 0.2f
        }
        
        // Keywords no nome do arquivo
        if (fileName.lowercase().contains("nubank")) confidence += 0.2f
        
        return confidence.coerceIn(0f, 1f)
    }
}
