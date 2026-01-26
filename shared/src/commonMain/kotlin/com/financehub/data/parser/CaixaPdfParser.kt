package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Caixa Econômica Federal em PDF.
 * 
 * Formato típico:
 * "25/01/2026  123456  PIX ENVIADO FULANO  -150,00  5.000,00"
 * Data | Documento | Histórico | Valor | Saldo
 * 
 * Keywords: caixa, cef, caixa economica
 */
class CaixaPdfParser : PdfParser() {
    
    override val bankIdentifier = "caixa_pdf"
    override val displayName = "Extrato Caixa (PDF)"
    override val dataSource = DataSource.CAIXA_BANK_STATEMENT
    
    override val contentKeywords = listOf("caixa", "cef", "caixa economica", "caixa federal")
    override val enableDebugLogs = true
    override val minLineLength = 15
    
    // Usar padrões centralizados do BrazilianPatterns
    // Padrão com documento: "25/01/2026  123456  Descrição  -150,00  5.000,00"
    private val withDocRegex = """^${BrazilianPatterns.DATE_BR_PATTERN}\s+(\d+)\s+(.+?)\s+${BrazilianPatterns.VALUE_ANY_PATTERN}(?:\s+${BrazilianPatterns.VALUE_ANY_PATTERN})?$""".toRegex()
    
    // Padrão simples
    private val simpleRegex = BrazilianPatterns.dateDescValueRegex()
    
    // Padrão com sinal explícito: "25/01/2026  Descrição  + 150,00" ou "- 150,00"
    private val withSignRegex = """^${BrazilianPatterns.DATE_BR_PATTERN}\s+(.+?)\s+([+-])\s*(\d{1,3}(?:[.,]\d{3})*[.,]\d{2})$""".toRegex()
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex].trim()
        
        // Padrão com documento
        withDocRegex.find(line)?.let { match ->
            return parseWithDoc(match, fileName, startIndex, line)
        }
        
        // Padrão com sinal explícito
        withSignRegex.find(line)?.let { match ->
            return parseWithSign(match, fileName, startIndex, line)
        }
        
        // Padrão simples
        simpleRegex.find(line)?.let { match ->
            return parseSimple(match, fileName, startIndex, line)
        }
        
        return null
    }
    
    private fun parseWithDoc(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        // groupValues[2] é o número do documento, ignoramos
        val description = match.groupValues[3].trim()
        val valueStr = match.groupValues[4]
        
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        val amount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        return createTransaction(fileName, lineIndex, date, description, amount, rawLine)
    }
    
    private fun parseWithSign(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        val description = match.groupValues[2].trim()
        val sign = match.groupValues[3]
        val valueStr = match.groupValues[4]
        
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        var amount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        if (sign == "-") amount = -kotlin.math.abs(amount)
        else amount = kotlin.math.abs(amount)
        
        return createTransaction(fileName, lineIndex, date, description, amount, rawLine)
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
        
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        val amount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        return createTransaction(fileName, lineIndex, date, description, amount, rawLine)
    }
    
    private fun createTransaction(
        fileName: String,
        lineIndex: Int,
        date: kotlinx.datetime.LocalDate,
        description: String,
        amount: Double,
        rawLine: String
    ): ParseLineResult {
        // Ignorar linhas de saldo
        if (description.lowercase().let { it.startsWith("saldo") || it == "s a l d o" }) {
            return ParseLineResult(
                Transaction(
                    id = "skip",
                    date = date,
                    description = "",
                    amount = 0.0,
                    category = Category.OUTROS,
                    source = dataSource,
                    rawData = ""
                ),
                lineIndex + 1
            )
        }
        
        val transaction = Transaction(
            id = generateIdWithDate(fileName, lineIndex, description, date.toString()),
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
               (lowerName.contains("caixa") || lowerName.contains("cef"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.lowercase().endsWith(".pdf")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        val fileNameLower = fileName.lowercase()
        
        if (contentLower.contains("caixa economica") || contentLower.contains("caixa federal")) {
            confidence += 0.5f
        }
        if (fileNameLower.contains("caixa") || fileNameLower.contains("cef")) {
            confidence += 0.3f
        }
        if (contentLower.contains("cef")) confidence += 0.2f
        
        return confidence.coerceIn(0f, 1f)
    }
}
