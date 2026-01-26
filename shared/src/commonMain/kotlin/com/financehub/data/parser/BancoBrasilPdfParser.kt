package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Banco do Brasil em PDF.
 * 
 * Formato típico:
 * "25/01/2026  PIX ENVIADO - FULANO DE TAL  -150,00  5.000,00"
 * Data | Histórico | Valor | Saldo
 * 
 * Keywords: banco do brasil, bb, banco brasil
 */
class BancoBrasilPdfParser : PdfParser() {
    
    override val bankIdentifier = "bb_pdf"
    override val displayName = "Extrato Banco do Brasil (PDF)"
    override val dataSource = DataSource.BANCO_BRASIL_STATEMENT
    
    override val contentKeywords = listOf("banco do brasil", "bb", "banco brasil", "bb s.a")
    override val enableDebugLogs = true
    override val minLineLength = 15
    
    // Usar padrões centralizados do BrazilianPatterns
    private val withBalanceRegex = BrazilianPatterns.dateDescValueBalanceRegex()
    private val simpleRegex = BrazilianPatterns.dateDescValueRegex()
    
    // Padrão com número de documento: "25/01/2026  123456789  Descrição  -150,00"
    private val withDocRegex = """^${BrazilianPatterns.DATE_BR_PATTERN}\s+(\d{6,})\s+(.+?)\s+${BrazilianPatterns.VALUE_ANY_PATTERN}$""".toRegex()
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex].trim()
        
        // Padrão com saldo
        withBalanceRegex.find(line)?.let { match ->
            return parseWithBalance(match, fileName, startIndex, line)
        }
        
        // Padrão com documento
        withDocRegex.find(line)?.let { match ->
            return parseWithDoc(match, fileName, startIndex, line)
        }
        
        // Padrão simples
        simpleRegex.find(line)?.let { match ->
            return parseSimple(match, fileName, startIndex, line)
        }
        
        return null
    }
    
    private fun parseWithBalance(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        val description = match.groupValues[2].trim()
        val valueStr = match.groupValues[3]
        // groupValues[4] é o saldo, ignoramos
        
        return parseAndCreate(dateStr, description, valueStr, fileName, lineIndex, rawLine)
    }
    
    private fun parseWithDoc(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        // groupValues[2] é o documento, ignoramos
        val description = match.groupValues[3].trim()
        val valueStr = match.groupValues[4]
        
        return parseAndCreate(dateStr, description, valueStr, fileName, lineIndex, rawLine)
    }
    
    private fun parseSimple(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        val description = match.groupValues[2].trim()
        val valueStr = match.groupValues[3]
        
        return parseAndCreate(dateStr, description, valueStr, fileName, lineIndex, rawLine)
    }
    
    private fun parseAndCreate(
        dateStr: String,
        description: String,
        valueStr: String,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        val amount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        // Ignorar linhas de saldo
        val descLower = description.lowercase()
        if (descLower.startsWith("saldo") || descLower == "s a l d o") {
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
        return lowerName.endsWith(".pdf") && 
               (lowerName.contains("bb") || lowerName.contains("bancodobrasil") ||
                lowerName.contains("banco_brasil") || lowerName.contains("banco-brasil"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.lowercase().endsWith(".pdf")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        val fileNameLower = fileName.lowercase()
        
        if (contentLower.contains("banco do brasil") || contentLower.contains("bb s.a")) {
            confidence += 0.5f
        }
        if (fileNameLower.contains("bb_") || fileNameLower.contains("bancodobrasil")) {
            confidence += 0.3f
        }
        if (contentLower.contains("agência") && contentLower.contains("conta")) {
            confidence += 0.1f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}
