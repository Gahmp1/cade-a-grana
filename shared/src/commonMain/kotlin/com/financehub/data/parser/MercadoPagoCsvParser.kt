package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.CsvParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato Mercado Pago (CSV).
 * 
 * Formato típico:
 * - Delimitador: ponto-e-vírgula ou vírgula
 * - Colunas: Data, Descrição, Tipo, Valor, Saldo
 * - Data: dd/MM/yyyy
 * - Valor: formato BR (1.234,56)
 */
class MercadoPagoCsvParser : CsvParser() {
    
    override val bankIdentifier = "mercadopago"
    override val displayName = "Extrato Mercado Pago"
    override val dataSource = DataSource.MERCADOPAGO_STATEMENT
    
    override val hasHeader = true
    override val minColumns = 3
    
    // Keywords para detecção automática
    override val contentKeywords = listOf("mercado pago", "mercadopago", "meli")
    override val headerPatterns = listOf("data;descrição;valor", "date;description;amount")
    
    // Detectar delimitador automaticamente
    private var detectedDelimiter = ";"
    
    override val delimiter: String
        get() = detectedDelimiter
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        detectDelimiter(fileContent.lines().firstOrNull() ?: "")
        return super.parse(fileContent, fileName)
    }
    
    private fun detectDelimiter(firstLine: String) {
        detectedDelimiter = when {
            firstLine.count { it == ';' } > firstLine.count { it == ',' } -> ";"
            else -> ","
        }
    }
    
    override fun parseRow(
        columns: List<String>,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): Transaction? {
        val dateStr = columns.getOrNull(0)?.trim() ?: return null
        val description = columns.getOrNull(1)?.trim() ?: return null
        
        // Valor pode estar em diferentes posições dependendo do formato
        val amountStr = findAmountColumn(columns) ?: return null
        
        if (dateStr.isEmpty() || description.isEmpty()) return null
        
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
    
    private fun findAmountColumn(columns: List<String>): String? {
        // Procurar coluna que parece ser valor monetário
        return columns.drop(2).firstOrNull { col ->
            val cleaned = col.trim()
            cleaned.isNotEmpty() && (
                cleaned.contains(",") || 
                cleaned.toDoubleOrNull() != null ||
                cleaned.replace(".", "").replace(",", ".").toDoubleOrNull() != null
            )
        }
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return (lowerName.endsWith(".csv") || lowerName.endsWith(".txt")) &&
               (lowerName.contains("mercado") || lowerName.contains("mp_") || 
                lowerName.contains("meli"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".txt")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        
        if (contentLower.contains("mercado") || contentLower.contains("meli")) {
            confidence += 0.5f
        }
        if (fileName.lowercase().contains("mercado")) confidence += 0.3f
        if (fileName.lowercase().contains("mp_")) confidence += 0.2f
        
        return confidence.coerceIn(0f, 1f)
    }
}
