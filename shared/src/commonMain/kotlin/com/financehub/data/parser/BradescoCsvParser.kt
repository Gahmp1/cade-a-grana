package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.CsvParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Bradesco (CSV).
 * 
 * Formato típico:
 * - Delimitador: ponto-e-vírgula
 * - Colunas: Data, Histórico, Documento, Crédito, Débito, Saldo
 * - Data: dd/MM/yyyy
 * - Crédito/Débito: colunas separadas, formato BR
 */
class BradescoCsvParser : CsvParser() {
    
    override val bankIdentifier = "bradesco"
    override val displayName = "Extrato Bradesco"
    override val dataSource = DataSource.BRADESCO_BANK_STATEMENT
    
    override val delimiter = ";"
    override val hasHeader = true
    override val minColumns = 4
    
    override val contentKeywords = listOf("bradesco", "bra_")
    override val headerPatterns = listOf(
        "data;histórico;documento;crédito;débito;saldo",
        "data;historico;documento;credito;debito;saldo",
        "data;descrição;valor"
    )
    
    // Índices das colunas (detectados dinamicamente)
    private var dateIndex = 0
    private var descriptionIndex = 1
    private var creditIndex = 3
    private var debitIndex = 4
    private var hasSeparateColumns = true
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        detectColumnLayout(fileContent.lines().firstOrNull() ?: "")
        return super.parse(fileContent, fileName)
    }
    
    private fun detectColumnLayout(headerLine: String) {
        val headerLower = headerLine.lowercase()
        val columns = headerLower.split(delimiter)
        
        hasSeparateColumns = columns.any { it.contains("crédit") || it.contains("credit") }
        
        dateIndex = columns.indexOfFirst { it.contains("data") }.takeIf { it >= 0 } ?: 0
        descriptionIndex = columns.indexOfFirst { 
            it.contains("histór") || it.contains("histor") || it.contains("descriç") 
        }.takeIf { it >= 0 } ?: 1
        
        if (hasSeparateColumns) {
            creditIndex = columns.indexOfFirst { it.contains("crédit") || it.contains("credit") }
                .takeIf { it >= 0 } ?: 3
            debitIndex = columns.indexOfFirst { it.contains("débit") || it.contains("debit") }
                .takeIf { it >= 0 } ?: 4
        }
    }
    
    override fun parseRow(
        columns: List<String>,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): Transaction? {
        val dateStr = columns.getOrNull(dateIndex)?.trim() ?: return null
        val description = columns.getOrNull(descriptionIndex)?.trim() ?: return null
        
        if (dateStr.isEmpty() || description.isEmpty()) return null
        
        val date = BrazilianPatterns.parseDateAuto(dateStr) ?: return null
        
        val amount = if (hasSeparateColumns) {
            parseAmountFromSeparateColumns(columns)
        } else {
            // Formato simples com valor único
            columns.getOrNull(2)?.let { BrazilianPatterns.parseValueAuto(it) }
        } ?: return null
        
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
    
    private fun parseAmountFromSeparateColumns(columns: List<String>): Double? {
        val creditStr = columns.getOrNull(creditIndex)?.trim() ?: ""
        val debitStr = columns.getOrNull(debitIndex)?.trim() ?: ""
        
        return when {
            creditStr.isNotBlank() -> BrazilianPatterns.parseValueAuto(creditStr)
            debitStr.isNotBlank() -> BrazilianPatterns.parseValueAuto(debitStr)?.let { -it }
            else -> null
        }
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return (lowerName.endsWith(".csv") || lowerName.endsWith(".txt")) &&
               lowerName.contains("bradesco")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".txt")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        
        if (contentLower.contains("bradesco")) confidence += 0.5f
        if (fileName.lowercase().contains("bradesco")) confidence += 0.3f
        
        headerPatterns.forEach { pattern ->
            if (contentLower.contains(pattern.lowercase())) confidence += 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}
