package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Bradesco em PDF.
 * 
 * Formato típico tabular:
 * "25/01/2026  PIX ENVIADO  -150,00  5.000,00"
 * Data | Histórico | Valor | Saldo
 * 
 * Keywords: bradesco, bra banco
 */
class BradescoPdfParser : PdfParser() {
    
    override val bankIdentifier = "bradesco_pdf"
    override val displayName = "Extrato Bradesco (PDF)"
    override val dataSource = DataSource.BRADESCO_BANK_STATEMENT
    
    override val contentKeywords = listOf("bradesco", "banco bradesco", "bra_")
    override val enableDebugLogs = true
    override val minLineLength = 15
    
    // Usar padrões centralizados do BrazilianPatterns
    private val lineWithDateRegex = BrazilianPatterns.dateDescValueBalanceRegex()
    private val simpleLineRegex = BrazilianPatterns.dateDescValueRegex()
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex].trim()
        
        // Tentar padrão com saldo
        lineWithDateRegex.find(line)?.let { match ->
            return parseTransaction(match, fileName, startIndex, line)
        }
        
        // Tentar padrão simples
        simpleLineRegex.find(line)?.let { match ->
            return parseTransaction(match, fileName, startIndex, line)
        }
        
        return null
    }
    
    private fun parseTransaction(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        val description = match.groupValues[2].trim()
        val valueStr = match.groupValues[3]
        
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        val amount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        // Ignorar linhas de saldo
        if (description.lowercase().contains("saldo") && 
            !description.lowercase().contains("resgate")) {
            return null
        }
        
        val transaction = Transaction(
            id = generateIdWithDate(fileName, lineIndex, description, dateStr),
            date = date,
            description = description,
            amount = amount,
            category = Category.classify(description),
            source = dataSource,
            rawData = rawLine
        )
        
        return ParseLineResult(transaction, lineIndex + 1)
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return lowerName.endsWith(".pdf") && lowerName.contains("bradesco")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.lowercase().endsWith(".pdf")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        val fileNameLower = fileName.lowercase()
        
        if (contentLower.contains("bradesco")) confidence += 0.5f
        if (fileNameLower.contains("bradesco")) confidence += 0.3f
        if (contentLower.contains("banco bradesco s.a")) confidence += 0.2f
        
        return confidence.coerceIn(0f, 1f)
    }
}
