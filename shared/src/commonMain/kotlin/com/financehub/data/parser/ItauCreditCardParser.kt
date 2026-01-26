package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.CsvParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para arquivos CSV de fatura do cartão Itaú.
 * 
 * Formato esperado:
 * - Header: "data,lançamento,valor"
 * - Data: yyyy-MM-dd (ISO)
 * - Valor: decimal simples (66.9)
 * - Delimitador: vírgula
 */
class ItauCreditCardParser : CsvParser() {
    
    override val bankIdentifier = "itau_cc"
    override val displayName = "Fatura Cartão Itaú"
    override val dataSource = DataSource.ITAU_CREDIT_CARD
    
    override val delimiter = ","
    override val hasHeader = true
    override val minColumns = 3
    
    override val dateColumnIndex = 0
    override val descriptionColumnIndex = 1
    override val amountColumnIndex = 2
    
    // Keywords para detecção automática
    override val contentKeywords = listOf("itau", "itaú", "fatura")
    override val headerPatterns = listOf("data,lançamento,valor", "data,lancamento,valor")
    
    override fun parseRow(
        columns: List<String>,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): Transaction? {
        val dateStr = columns.getOrNull(dateColumnIndex)?.trim() ?: return null
        val description = columns.getOrNull(descriptionColumnIndex)?.trim() ?: return null
        val amountStr = columns.getOrNull(amountColumnIndex)?.trim()
            ?.replace("R$", "")?.trim() ?: return null
        
        // Validar campos não vazios
        if (dateStr.isEmpty() || description.isEmpty() || amountStr.isEmpty()) {
            return null
        }
        
        // Validar formato de data (aceita dd/MM/yyyy ou yyyy-MM-dd)
        if (!BrazilianPatterns.DATE_BR.matches(dateStr) && 
            !BrazilianPatterns.DATE_ISO.matches(dateStr)) {
            return null
        }
        
        // Parsear data
        val date = BrazilianPatterns.parseDateAuto(dateStr) ?: return null
        
        // Parsear valor (formato decimal simples: 66.9)
        val rawAmount = BrazilianPatterns.parseValueDecimal(amountStr) ?: return null
        
        // No cartão de crédito:
        // - Valores positivos são GASTOS (saída) -> converter para negativo
        // - Valores negativos são ESTORNOS/PAGAMENTOS (entrada) -> manter positivo
        val amount = -rawAmount
        
        val category = Category.classify(description)
        
        return Transaction(
            id = generateIdWithDate(fileName, lineIndex, description, dateStr),
            date = date,
            description = description,
            amount = amount,
            category = category,
            source = dataSource,
            rawData = rawLine
        )
    }
    
    override fun supports(fileName: String): Boolean {
        // Ignorar relatórios gerados
        if (fileName.startsWith("relatorio")) return false
        
        val lowerName = fileName.lowercase()
        
        // Aceitar ambos formatos: "Itau-fatura..." ou "fatura-itau..."
        return lowerName.endsWith(".csv") &&
               lowerName.contains("itau") &&
               (lowerName.contains("fatura") || lowerName.contains("cartao") || lowerName.contains("cartão")) &&
               !lowerName.contains("extrato")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        val extension = fileName.substringAfterLast(".").lowercase()
        if (extension != "csv") return 0f
        
        var confidence = 0f
        val firstLine = contentPreview.lines().firstOrNull()?.lowercase() ?: ""
        
        // Header exato = alta confiança
        if (firstLine.contains("data,lançamento,valor") || 
            firstLine.contains("data,lancamento,valor")) {
            confidence += 0.6f
        }
        
        // Keywords no nome do arquivo
        val lowerName = fileName.lowercase()
        if (lowerName.contains("itau")) confidence += 0.2f
        if (lowerName.contains("fatura")) confidence += 0.1f
        if (lowerName.contains("cartao") || lowerName.contains("cartão")) confidence += 0.1f
        
        return confidence.coerceIn(0f, 1f)
    }
}
