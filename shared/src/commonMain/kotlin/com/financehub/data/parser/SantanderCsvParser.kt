package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.CsvParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato/fatura Santander (CSV).
 * 
 * Formato típico:
 * - Delimitador: ponto-e-vírgula
 * - Colunas: Data, Descrição, Valor, Saldo (extrato) ou Data, Descrição, Valor (fatura)
 * - Data: dd/MM/yyyy
 * - Valor: formato BR (1.234,56)
 */
class SantanderCsvParser : CsvParser() {
    
    override val bankIdentifier = "santander"
    override val displayName = "Extrato/Fatura Santander"
    override val dataSource = DataSource.SANTANDER_BANK_STATEMENT
    
    override val delimiter = ";"
    override val hasHeader = true
    override val minColumns = 3
    
    override val contentKeywords = listOf("santander")
    override val headerPatterns = listOf(
        "data;descrição;valor",
        "data;descricao;valor",
        "data;histórico;valor;saldo"
    )
    
    private var dateIndex = 0
    private var descriptionIndex = 1
    private var amountIndex = 2
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        detectColumnLayout(fileContent.lines().firstOrNull() ?: "")
        return super.parse(fileContent, fileName)
    }
    
    private fun detectColumnLayout(headerLine: String) {
        val columns = headerLine.lowercase().split(delimiter)
        
        dateIndex = columns.indexOfFirst { it.contains("data") }.takeIf { it >= 0 } ?: 0
        descriptionIndex = columns.indexOfFirst { 
            it.contains("descriç") || it.contains("descric") || it.contains("histór")
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
        val amount = BrazilianPatterns.parseValueAuto(amountStr) ?: return null
        
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
        return (lowerName.endsWith(".csv") || lowerName.endsWith(".txt")) &&
               lowerName.contains("santander")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".txt")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        
        if (contentLower.contains("santander")) confidence += 0.5f
        if (fileName.lowercase().contains("santander")) confidence += 0.3f
        
        headerPatterns.forEach { pattern ->
            if (contentLower.contains(pattern.lowercase())) confidence += 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}
