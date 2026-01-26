package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Parser para extrato Santander em PDF.
 * 
 * Formato típico:
 * "25/01  PIX ENVIADO  -150,00"
 * ou
 * "25/01/2026  Transferência  R$ 150,00  D"  (D=Débito, C=Crédito)
 * 
 * Keywords: santander, banco santander
 */
class SantanderPdfParser : PdfParser() {
    
    override val bankIdentifier = "santander_pdf"
    override val displayName = "Extrato Santander (PDF)"
    override val dataSource = DataSource.SANTANDER_BANK_STATEMENT
    
    override val contentKeywords = listOf("santander", "banco santander", "santander brasil")
    override val enableDebugLogs = true
    override val minLineLength = 10
    
    // Usar padrões centralizados do BrazilianPatterns
    // Data completa: "25/01/2026  Descrição  -150,00"
    private val fullDateRegex = """^${BrazilianPatterns.DATE_BR_PATTERN}\s+(.+?)\s+${BrazilianPatterns.VALUE_ANY_PATTERN}(?:\s+[DC])?$""".toRegex()
    
    // Data curta: "25/01  Descrição  -150,00" (ano inferido)
    private val shortDateRegex = """^${BrazilianPatterns.DATE_SHORT_PATTERN}\s+(.+?)\s+${BrazilianPatterns.VALUE_ANY_PATTERN}(?:\s+[DC])?$""".toRegex()
    
    // Padrão com R$: "25/01/2026  Descrição  R$ 150,00  D"
    private val withCurrencyRegex = """^(\d{2}/\d{2}/?\d{0,4})\s+(.+?)\s+${BrazilianPatterns.VALUE_CURRENCY_PATTERN}\s+([DC])$""".toRegex()
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex].trim()
        
        // Padrão com R$ e indicador D/C
        withCurrencyRegex.find(line)?.let { match ->
            return parseWithIndicator(match, fileName, startIndex, line)
        }
        
        // Data completa
        fullDateRegex.find(line)?.let { match ->
            return parseFullDate(match, fileName, startIndex, line)
        }
        
        // Data curta
        shortDateRegex.find(line)?.let { match ->
            return parseShortDate(match, fileName, startIndex, line)
        }
        
        return null
    }
    
    private fun parseWithIndicator(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = normalizeDate(match.groupValues[1])
        val description = match.groupValues[2].trim()
        val valueStr = match.groupValues[3]
        val indicator = match.groupValues[4] // D = Débito, C = Crédito
        
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        var amount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        // D = débito (negativo), C = crédito (positivo)
        if (indicator == "D") amount = -kotlin.math.abs(amount)
        else amount = kotlin.math.abs(amount)
        
        return createTransaction(fileName, lineIndex, date, description, amount, rawLine)
    }
    
    private fun parseFullDate(
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
    
    private fun parseShortDate(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val shortDate = match.groupValues[1]
        // Inferir ano atual para datas curtas (dd/MM)
        val currentYear = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).year
        val dateStr = "$shortDate/$currentYear"
        val description = match.groupValues[2].trim()
        val valueStr = match.groupValues[3]
        
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        val amount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        return createTransaction(fileName, lineIndex, date, description, amount, rawLine)
    }
    
    private fun normalizeDate(dateStr: String): String {
        val currentYear = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).year
        return if (dateStr.length == 5) "$dateStr/$currentYear" else dateStr
    }
    
    private fun createTransaction(
        fileName: String,
        lineIndex: Int,
        date: kotlinx.datetime.LocalDate,
        description: String,
        amount: Double,
        rawLine: String
    ): ParseLineResult {
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
        return lowerName.endsWith(".pdf") && lowerName.contains("santander")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.lowercase().endsWith(".pdf")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        
        if (contentLower.contains("santander")) confidence += 0.5f
        if (fileName.lowercase().contains("santander")) confidence += 0.3f
        if (contentLower.contains("banco santander")) confidence += 0.2f
        
        return confidence.coerceIn(0f, 1f)
    }
}
