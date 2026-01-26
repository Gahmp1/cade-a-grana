package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Banco do Brasil em PDF.
 * 
 * Formato típico do app BB (novo):
 * "02/01/2026 60,00 (-)99020 981072403000353"
 * "Telefone Pre-Pago"
 * "01/01 00:19 (61)98107-2403 Tim"
 * 
 * Onde:
 * - Data: DD/MM/YYYY
 * - Valor: N.NNN,NN
 * - Sinal: (+) ou (-)
 * - Documento: 99020
 * - Código: longo número
 * - Descrição na linha seguinte
 * 
 * Keywords: banco do brasil, bb, banco brasil, extrato de conta
 */
class BancoBrasilPdfParser : PdfParser() {
    
    override val bankIdentifier = "bb_pdf"
    override val displayName = "Extrato Banco do Brasil (PDF)"
    override val dataSource = DataSource.BANCO_BRASIL_STATEMENT
    
    override val contentKeywords = listOf("banco do brasil", "bb", "banco brasil", "bb s.a", "extrato de conta")
    override val enableDebugLogs = true
    override val minLineLength = 10
    
    // Novo formato BB App: "02/01/2026 60,00 (-)99020 981072403000353"
    // Data + Valor + Sinal(+/-) + Documento + Código
    private val bbAppLineRegex = """^(\d{2}/\d{2}/\d{4})\s+([\d.,]+)\s*\(([+-])\)(\d+)\s+(\d+)$""".toRegex()
    
    // Formato antigo: "25/01/2026  PIX ENVIADO  -150,00  5.000,00"
    private val oldFormatRegex = BrazilianPatterns.dateDescValueBalanceRegex()
    private val simpleRegex = BrazilianPatterns.dateDescValueRegex()
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex].trim()
        
        // Novo formato BB App
        bbAppLineRegex.find(line)?.let { match ->
            return parseNewBBFormat(match, lines, startIndex, fileName)
        }
        
        // Formato antigo com saldo
        oldFormatRegex.find(line)?.let { match ->
            return parseOldFormat(match, fileName, startIndex, line)
        }
        
        // Formato simples
        simpleRegex.find(line)?.let { match ->
            return parseSimpleFormat(match, fileName, startIndex, line)
        }
        
        return null
    }
    
    /**
     * Parse do novo formato do BB App:
     * Linha 1: "02/01/2026 60,00 (-)99020 981072403000353"
     * Linha 2: "Telefone Pre-Pago" (descrição)
     * Linha 3: "01/01 00:19 (61)98107-2403 Tim" (detalhes opcionais)
     */
    private fun parseNewBBFormat(
        match: MatchResult,
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        val valueStr = match.groupValues[2]
        val sign = match.groupValues[3]
        // match.groupValues[4] é o documento (99020)
        // match.groupValues[5] é o código longo
        
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        
        // Parse valor com sinal
        val absValue = BrazilianPatterns.parseValueBR(valueStr) ?: return null
        val amount = if (sign == "-") -absValue else absValue
        
        // Buscar descrição na próxima linha
        var description = "Transação BB"
        var nextLineIndex = startIndex + 1
        
        if (nextLineIndex < lines.size) {
            val nextLine = lines[nextLineIndex].trim()
            // Verificar se não é outra transação ou linha de saldo
            if (nextLine.isNotBlank() && 
                !bbAppLineRegex.matches(nextLine) &&
                !nextLine.startsWith("Saldo") &&
                !nextLine.contains("S A L D O") &&
                !nextLine.lowercase().contains("agência") &&
                nextLine.length > 3) {
                description = nextLine
                nextLineIndex++
            }
        }
        
        // Ignorar linhas de saldo
        val descLower = description.lowercase()
        if (descLower.startsWith("saldo") || descLower == "s a l d o" || descLower.contains("saldo anterior")) {
            return null
        }
        
        val rawLine = lines[startIndex] + (if (nextLineIndex > startIndex + 1) " | $description" else "")
        
        val transaction = Transaction(
            id = generateIdWithDate(fileName, startIndex, description, dateStr),
            date = date,
            description = description,
            amount = amount,
            category = Category.classify(description),
            source = dataSource,
            rawData = rawLine
        )
        
        return ParseLineResult(transaction, nextLineIndex)
    }
    
    private fun parseOldFormat(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        val description = match.groupValues[2].trim()
        val valueStr = match.groupValues[3]
        
        return createTransaction(dateStr, description, valueStr, fileName, lineIndex, rawLine)
    }
    
    private fun parseSimpleFormat(
        match: MatchResult,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): ParseLineResult? {
        val dateStr = match.groupValues[1]
        val description = match.groupValues[2].trim()
        val valueStr = match.groupValues[3]
        
        return createTransaction(dateStr, description, valueStr, fileName, lineIndex, rawLine)
    }
    
    private fun createTransaction(
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
                lowerName.contains("banco_brasil") || lowerName.contains("banco-brasil") ||
                lowerName.contains("extrato-bb"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.lowercase().endsWith(".pdf")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        val fileNameLower = fileName.lowercase()
        
        // Detectar pelo conteúdo
        if (contentLower.contains("banco do brasil") || contentLower.contains("bb s.a")) {
            confidence += 0.4f
        }
        if (contentLower.contains("extrato de conta corrente")) {
            confidence += 0.3f
        }
        if (contentLower.contains("agência") && contentLower.contains("conta")) {
            confidence += 0.1f
        }
        
        // Detectar pelo nome do arquivo
        if (fileNameLower.contains("bb") || fileNameLower.contains("bancodobrasil") || 
            fileNameLower.contains("extrato-bb")) {
            confidence += 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}
