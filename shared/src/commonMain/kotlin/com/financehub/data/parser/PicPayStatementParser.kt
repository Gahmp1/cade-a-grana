package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato PicPay (PDF).
 * 
 * Formato do PDF extraído:
 * - Linha com data: dd/MM/yyyy
 * - Próxima linha: HH:MM:SS Descrição R$ Valor R$ Saldo R$ SaldoSacavel
 */
class PicPayStatementParser : PdfParser() {
    
    override val bankIdentifier = "picpay"
    override val displayName = "Extrato PicPay"
    override val dataSource = DataSource.PICPAY_STATEMENT
    
    // Keywords para detecção automática
    override val contentKeywords = listOf("picpay", "pic pay", "pix recebido", "pix enviado")
    
    override val enableDebugLogs = true
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex]
        val trimmedLine = line.trim()
        
        // Verificar se a linha contém apenas data (dd/MM/yyyy)
        if (!BrazilianPatterns.isValidDateBR(trimmedLine)) {
            return null
        }
        
        // Próxima linha deve ter a transação
        if (startIndex + 1 >= lines.size) {
            return null
        }
        
        val nextLine = lines[startIndex + 1]
        val valueResult = BrazilianPatterns.extractFirstValueAuto(nextLine) ?: return null
        
        val dateStr = trimmedLine
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        val rawAmount = valueResult.first
        
        // Descrição é o texto antes do valor
        val valueStartIndex = valueResult.second.first
        var description = nextLine.substring(0, valueStartIndex).trim()
        
        // Remover hora (HH:MM:SS) do início da descrição
        description = BrazilianPatterns.removeLeadingTime(description)
        
        // Remover "R$" se sobrou
        description = description.replace("R$", "").trim()
        
        // Validar descrição mínima
        if (description.length < 3) {
            return null
        }
        
        // Determinar se é entrada ou saída
        val isIncome = isIncomeTransaction(description)
        val amount = if (isIncome) rawAmount else -rawAmount
        
        val category = Category.classify(description)
        
        val transaction = Transaction(
            id = generateIdWithDate(fileName, startIndex, description, dateStr),
            date = date,
            description = description,
            amount = amount,
            category = category,
            source = dataSource,
            rawData = "$dateStr | $nextLine"
        )
        
        // Pular 2 linhas (data + transação)
        return ParseLineResult(transaction, startIndex + 2)
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return lowerName.endsWith(".pdf") && 
               lowerName.contains("picpay")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        val extension = fileName.substringAfterLast(".").lowercase()
        if (extension != "pdf") return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        
        // Keywords específicas do PicPay
        if (contentLower.contains("picpay")) confidence += 0.5f
        if (contentLower.contains("@")) confidence += 0.1f // Username PicPay
        if (contentLower.contains("cofrinho")) confidence += 0.1f
        if (contentLower.contains("pix enviado") || contentLower.contains("pix recebido")) {
            confidence += 0.2f
        }
        
        // Nome do arquivo
        val lowerName = fileName.lowercase()
        if (lowerName.contains("picpay")) confidence += 0.1f
        
        return confidence.coerceIn(0f, 1f)
    }
}
