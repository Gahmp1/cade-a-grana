package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.CsvParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Banco do Brasil (CSV).
 * 
 * Formato típico:
 * - Delimitador: ponto-e-vírgula ou vírgula
 * - Colunas: Data, Histórico, Documento, Valor, Saldo
 * - Data: dd/MM/yyyy
 * - Valor: formato BR (1.234,56)
 */
class BancoBrasilCsvParser : CsvParser() {
    
    override val bankIdentifier = "bancodobrasil"
    override val displayName = "Extrato Banco do Brasil"
    override val dataSource = DataSource.BANCO_BRASIL_STATEMENT
    
    override val hasHeader = true
    override val minColumns = 3
    
    override val contentKeywords = listOf("banco do brasil", "bb", "bb_")
    override val headerPatterns = listOf(
        "data;histórico;valor",
        "data;descricao;valor;saldo",
        "data lançamento;histórico;valor"
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
            headerLine.count { it == ';' } >= 2 -> ";"
            headerLine.count { it == ',' } >= 2 -> ","
            else -> ";"
        }
        
        val columns = headerLine.lowercase().split(detectedDelimiter)
        
        // Encontrar índices das colunas
        dateIndex = columns.indexOfFirst { 
            it.contains("data") 
        }.takeIf { it >= 0 } ?: 0
        
        descriptionIndex = columns.indexOfFirst { 
            it.contains("histór") || it.contains("histor") || it.contains("descriç")
        }.takeIf { it >= 0 } ?: 1
        
        amountIndex = columns.indexOfFirst { 
            it.contains("valor") 
        }.takeIf { it >= 0 } ?: 2
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
               (lowerName.contains("bb") || lowerName.contains("bancodobrasil") ||
                lowerName.contains("banco_brasil") || lowerName.contains("banco-brasil"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".txt")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        val fileNameLower = fileName.lowercase()
        
        // Verificar conteúdo
        if (contentLower.contains("banco do brasil") || contentLower.contains("bb")) {
            confidence += 0.5f
        }
        
        // Verificar nome do arquivo
        if (fileNameLower.contains("bb_") || fileNameLower.contains("bancodobrasil") ||
            fileNameLower.contains("banco_brasil") || fileNameLower.contains("banco-brasil")) {
            confidence += 0.3f
        }
        
        // Verificar headers
        headerPatterns.forEach { pattern ->
            if (contentLower.contains(pattern.lowercase())) confidence += 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}
