package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.DelimitedTextParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato bancário Itaú (TXT/CSV).
 * 
 * Formato esperado:
 * - Delimitador: ponto-e-vírgula (;)
 * - Data: dd/MM/yyyy
 * - Valor: formato BR com vírgula decimal (1.234,56)
 * - Sem header
 */
class ItauBankStatementParser : DelimitedTextParser() {
    
    override val bankIdentifier = "itau_bank"
    override val displayName = "Extrato Bancário Itaú"
    override val dataSource = DataSource.ITAU_BANK_STATEMENT
    
    override val delimiter = ";"
    override val hasHeader = false
    override val minColumns = 3
    
    override val dateColumnIndex = 0
    override val descriptionColumnIndex = 1
    override val amountColumnIndex = 2
    
    // Keywords para detecção automática
    override val contentKeywords = listOf("itau", "itaú", "extrato")
    
    override fun parseRow(
        columns: List<String>,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): Transaction? {
        val dateStr = columns.getOrNull(dateColumnIndex)?.trim() ?: return null
        val description = columns.getOrNull(descriptionColumnIndex)?.trim() ?: return null
        val amountStr = columns.getOrNull(amountColumnIndex)?.trim() ?: return null
        
        // Validar campos não vazios
        if (dateStr.isEmpty() || description.isEmpty() || amountStr.isEmpty()) {
            return null
        }
        
        // Validar formato de data (dd/MM/yyyy)
        if (!BrazilianPatterns.DATE_BR.matches(dateStr)) {
            return null
        }
        
        // Parsear data
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        
        // Parsear valor (detecta automaticamente BR ou decimal)
        val amount = BrazilianPatterns.parseValueAuto(amountStr) ?: return null
        
        // No extrato bancário:
        // - Valores positivos são ENTRADAS (receita)
        // - Valores negativos são SAÍDAS (gastos)
        
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
        
        // Aceitar ambos formatos: "Itau-extrato..." ou "extrato-itau..."
        return (lowerName.endsWith(".txt") || lowerName.endsWith(".csv")) &&
               lowerName.contains("itau") &&
               lowerName.contains("extrato")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        val extension = fileName.substringAfterLast(".").lowercase()
        if (extension != "txt" && extension != "csv") return 0f
        
        var confidence = 0f
        
        // Verificar se as linhas seguem o padrão dd/MM/yyyy;descrição;valor
        val lines = contentPreview.lines().take(5)
        val patternMatches = lines.count { line ->
            line.contains(";") && BrazilianPatterns.DATE_BR.containsMatchIn(line)
        }
        confidence += (patternMatches.toFloat() / 5) * 0.5f
        
        // Keywords no nome do arquivo
        val lowerName = fileName.lowercase()
        if (lowerName.contains("itau")) confidence += 0.3f
        if (lowerName.contains("extrato")) confidence += 0.2f
        
        return confidence.coerceIn(0f, 1f)
    }
}
