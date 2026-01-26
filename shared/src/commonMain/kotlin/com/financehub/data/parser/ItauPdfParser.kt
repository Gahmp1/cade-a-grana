package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Parser para extrato bancário Itaú em formato PDF.
 * 
 * Formato do PDF extraído (baseado na estrutura de tabela):
 * - Tabela com colunas: Data, Descrição, Valor, Saldo
 * - Data: dd/MM/yyyy
 * - Valor: R$ 1.234,56 (formato BR)
 * 
 * Estratégia: Procurar linhas que contenham data + descrição + valor monetário
 */
class ItauPdfParser : PdfParser() {
    
    override val bankIdentifier = "itau_pdf"
    override val displayName = "Extrato Itaú (PDF)"
    override val dataSource = DataSource.ITAU_BANK_STATEMENT
    
    // Keywords para detecção automática
    override val contentKeywords = listOf("itau", "itaú", "banco itaú", "uniclass")
    
    override val enableDebugLogs = true
    override val minLineLength = 15
    
    // Usar padrões centralizados do BrazilianPatterns
    private val dateAtStartRegex = """^${BrazilianPatterns.DATE_BR_PATTERN}\s+(.+)""".toRegex()
    private val valueWithSignRegex = """(-?)R?\$?\s*(\d{1,3}(?:[.,]\d{3})*[.,]\d{2}|\d+[.,]\d{2})""".toRegex()
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex].trim()
        
        // Tentar encontrar data no início da linha
        val dateMatch = dateAtStartRegex.find(line) ?: return null
        
        val dateStr = dateMatch.groupValues[1]
        val restOfLine = dateMatch.groupValues[2].trim()
        
        // Validar data
        val date = BrazilianPatterns.parseDateBR(dateStr) ?: return null
        
        // Procurar valores monetários no resto da linha
        val valueMatches = valueWithSignRegex.findAll(restOfLine).toList()
        if (valueMatches.isEmpty()) return null
        
        // Primeiro valor geralmente é o valor da transação
        val valueMatch = valueMatches.first()
        val isNegative = valueMatch.groupValues[1] == "-"
        val valueStr = valueMatch.groupValues[2]
        
        val rawAmount = BrazilianPatterns.parseValueAuto(valueStr) ?: return null
        
        // Descrição é o texto antes do primeiro valor
        val description = restOfLine
            .substring(0, valueMatch.range.first)
            .trim()
            .replace(Regex("\\s+"), " ") // Normalizar espaços
        
        // Ignorar linhas de saldo ou headers
        if (shouldIgnoreDescription(description)) {
            return null
        }
        
        // Validar descrição mínima
        if (description.length < 3) {
            return null
        }
        
        // Determinar sinal baseado no contexto
        val amount = when {
            isNegative -> -rawAmount
            isIncomeTransaction(description) -> rawAmount
            else -> -rawAmount // Por padrão, é gasto
        }
        
        val category = Category.classify(description)
        
        val transaction = Transaction(
            id = generateIdWithDate(fileName, startIndex, description, dateStr),
            date = date,
            description = description,
            amount = amount,
            category = category,
            source = dataSource,
            rawData = line
        )
        
        return ParseLineResult(transaction, startIndex + 1)
    }
    
    /**
     * Verifica se a descrição deve ser ignorada (headers, totais, etc.)
     */
    private fun shouldIgnoreDescription(description: String): Boolean {
        val lowerDesc = description.lowercase()
        return IGNORE_PATTERNS.any { pattern -> lowerDesc.contains(pattern) }
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return lowerName.endsWith(".pdf") && 
               lowerName.contains("itau") &&
               lowerName.contains("extrato")
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        val extension = fileName.substringAfterLast(".").lowercase()
        if (extension != "pdf") return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        
        // Keywords específicas do Itaú
        if (contentLower.contains("itaú") || contentLower.contains("itau")) confidence += 0.3f
        if (contentLower.contains("uniclass")) confidence += 0.2f
        if (contentLower.contains("agência") || contentLower.contains("agencia")) confidence += 0.1f
        if (contentLower.contains("conta corrente")) confidence += 0.1f
        
        // Verificar se tem padrão de data BR na primeira parte
        val hasDatePattern = BrazilianPatterns.DATE_BR.containsMatchIn(contentPreview.take(500))
        if (hasDatePattern) confidence += 0.2f
        
        // Nome do arquivo
        val lowerName = fileName.lowercase()
        if (lowerName.contains("itau")) confidence += 0.1f
        
        return confidence.coerceIn(0f, 1f)
    }
    
    companion object {
        /**
         * Padrões de texto que devem ser ignorados
         */
        val IGNORE_PATTERNS = listOf(
            "saldo anterior", "saldo inicial", "saldo final",
            "saldo disponível", "saldo disponivel",
            "total", "subtotal",
            "data/hora", "descrição das movimentações", "descricao",
            "valor saldo", "saldo sacável",
            "lançamento", "lancamento",
            "crédito", "débito",
            "página", "pagina"
        )
    }
}
