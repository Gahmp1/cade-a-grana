package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.CsvParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Caixa Econômica Federal (CSV/TXT).
 * 
 * Formato típico:
 * - Delimitador: ponto-e-vírgula ou tabulação
 * - Colunas: Data, Histórico/Descrição, Documento, Valor, Saldo
 * - Data: dd/MM/yyyy
 * - Valor: formato BR (1.234,56) ou com sinal (+ / -)
 */
class CaixaCsvParser : CsvParser() {
    
    override val bankIdentifier = "caixa"
    override val displayName = "Extrato Caixa"
    override val dataSource = DataSource.CAIXA_BANK_STATEMENT
    
    override val hasHeader = true
    override val minColumns = 3
    
    override val contentKeywords = listOf("caixa", "cef", "caixa economica")
    override val headerPatterns = listOf(
        "data;histórico;valor",
        "data;descrição;valor;saldo",
        "data\thistórico\tvalor"
    )
    
    private var detectedDelimiter = ";"
    override val delimiter: String
        get() = detectedDelimiter
    
    private var dateIndex = 0
    private var descriptionIndex = 1
    private var amountIndex = 2
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        val firstLine = fileContent.lines().firstOrNull() ?: ""
        detectDelimiterAndLayout(firstLine)
        return super.parse(fileContent, fileName)
    }
    
    private fun detectDelimiterAndLayout(headerLine: String) {
        // Detectar delimitador
        detectedDelimiter = when {
            headerLine.count { it == '\t' } >= 2 -> "\t"
            headerLine.count { it == ';' } >= 2 -> ";"
            else -> ","
        }
        
        val columns = headerLine.lowercase().split(detectedDelimiter)
        
        dateIndex = columns.indexOfFirst { it.contains("data") }.takeIf { it >= 0 } ?: 0
        descriptionIndex = columns.indexOfFirst { 
            it.contains("histór") || it.contains("histor") || it.contains("descriç")
        }.takeIf { it >= 0 } ?: 1
        amountIndex = columns.indexOfFirst { it.contains("valor") }.takeIf { it >= 0 } ?: 2
    }
    
    override fun parseRow(
        columns: List<String>,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): Transaction? {
        val dateStr = columns.getOrNull(dateIndex)?.trim() ?: return null
        val description = columns.getOrNull(descriptionIndex)?.trim() ?: return null
        val amountStr = columns.getOrNull(amountIndex)?.trim() ?: return null
        
        if (dateStr.isEmpty() || description.isEmpty() || amountStr.isEmpty()) return null
        
        val date = BrazilianPatterns.parseDateAuto(dateStr) ?: return null
        val amount = parseAmountWithSign(amountStr) ?: return null
        
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
    
    private fun parseAmountWithSign(amountStr: String): Double? {
        val cleaned = amountStr.trim()
        
        // Verificar sinal explícito
        return when {
            cleaned.startsWith("+") -> 
                BrazilianPatterns.parseValueAuto(cleaned.drop(1))
            cleaned.startsWith("-") -> 
                BrazilianPatterns.parseValueAuto(cleaned.drop(1))?.let { -it }
            else -> 
                BrazilianPatterns.parseValueAuto(cleaned)
        }
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return (lowerName.endsWith(".csv") || lowerName.endsWith(".txt")) &&
               (lowerName.contains("caixa") || lowerName.contains("cef"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".txt")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        
        if (contentLower.contains("caixa") || contentLower.contains("cef")) {
            confidence += 0.5f
        }
        if (fileName.lowercase().let { it.contains("caixa") || it.contains("cef") }) {
            confidence += 0.3f
        }
        
        headerPatterns.forEach { pattern ->
            if (contentLower.contains(pattern.lowercase())) confidence += 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}
