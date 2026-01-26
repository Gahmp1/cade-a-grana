package com.financehub.data.parser

import com.financehub.data.parser.common.BrazilianPatterns
import com.financehub.data.parser.common.PdfParser
import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction
import kotlinx.datetime.LocalDate

/**
 * Parser para extrato Nubank em PDF.
 * 
 * Formato real do PDF (extraído via PDFBox):
 * - Data em formato texto: "02 JAN 2026", "03 JAN 2026"
 * - Transações nas linhas seguintes: "Compra no débito via NuPay iFood 119,99"
 * - Linhas de "Total de saídas" e "Total de entradas" indicam sinal
 * 
 * Estratégia:
 * 1. Detectar linhas de data (ex: "02 JAN 2026 Total de saídas - 119,99")
 * 2. Ler transações das linhas seguintes até próxima data
 * 3. Usar contexto (saídas = negativo, entradas = positivo)
 */
class NubankPdfParser : PdfParser() {
    
    override val bankIdentifier = "nubank_pdf"
    override val displayName = "Extrato Nubank (PDF)"
    override val dataSource = DataSource.NUBANK_BANK_STATEMENT
    
    override val contentKeywords = listOf("nubank", "nu pagamentos", "roxinho", "nu_")
    override val enableDebugLogs = true
    override val minLineLength = 5
    
    // Mapeamento de meses em português
    private val monthMap = mapOf(
        "JAN" to 1, "FEV" to 2, "MAR" to 3, "ABR" to 4,
        "MAI" to 5, "JUN" to 6, "JUL" to 7, "AGO" to 8,
        "SET" to 9, "OUT" to 10, "NOV" to 11, "DEZ" to 12
    )
    
    // Regex para data no formato "02 JAN 2026"
    private val dateTextRegex = """^(\d{2})\s+(JAN|FEV|MAR|ABR|MAI|JUN|JUL|AGO|SET|OUT|NOV|DEZ)\s+(\d{4})""".toRegex(RegexOption.IGNORE_CASE)
    
    // Regex para linha de transação: "Descrição 123,45" ou "Descrição 1.234,56"
    private val transactionLineRegex = """^(.+?)\s+(\d{1,3}(?:\.\d{3})*,\d{2})$""".toRegex()
    
    // Regex para valor monetário sozinho ou no final de uma linha
    private val valueOnlyRegex = """^(\d{1,3}(?:\.\d{3})*,\d{2})$""".toRegex()
    private val valueAtEndRegex = """(\d{1,3}(?:\.\d{3})*,\d{2})$""".toRegex()
    
    // Padrões que indicam início de transação (para linhas quebradas)
    private val transactionStartPatterns = listOf(
        "transferência recebida",
        "transferência enviada", 
        "pix recebido",
        "pix enviado",
        "compra no débito",
        "pagamento de boleto",
        "aplicação rdb",
        "resgate rdb",
        "reembolso",
        "devolução",
        "ted recebida",
        "ted enviada",
        "saque",
        "depósito"
    )
    
    // Regex para detectar total de saídas/entradas (para pegar o contexto do sinal)
    private val totalSaidasRegex = """Total de saídas""".toRegex(RegexOption.IGNORE_CASE)
    private val totalEntradasRegex = """Total de entradas""".toRegex(RegexOption.IGNORE_CASE)
    
    // Estado do parser: mantém a data atual e se é saída ou entrada
    private var currentDate: LocalDate? = null
    private var isExpense: Boolean = true  // Padrão: saídas são negativas
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        // Resetar estado
        currentDate = null
        isExpense = true
        
        return super.parse(fileContent, fileName)
    }
    
    override fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult? {
        val line = lines[startIndex].trim()
        
        // Ignorar linhas conhecidas de cabeçalho/rodapé primeiro
        if (shouldIgnoreLine(line)) {
            return null
        }
        
        // Tentar detectar nova data
        dateTextRegex.find(line)?.let { match ->
            val day = match.groupValues[1].toIntOrNull() ?: return@let
            val monthStr = match.groupValues[2].uppercase()
            val year = match.groupValues[3].toIntOrNull() ?: return@let
            val month = monthMap[monthStr] ?: return@let
            
            try {
                currentDate = LocalDate(year, month, day)
            } catch (e: Exception) {
                // Ignorar data inválida
            }
            
            // Verificar se essa linha também tem "Total de saídas" ou "Total de entradas"
            isExpense = when {
                totalEntradasRegex.containsMatchIn(line) -> false
                totalSaidasRegex.containsMatchIn(line) -> true
                else -> isExpense // Manter contexto anterior
            }
            
            // Data detectada, mas não é uma transação em si
            return null
        }
        
        // Verificar se é linha de total (atualiza o contexto)
        if (totalSaidasRegex.containsMatchIn(line)) {
            isExpense = true
            return null
        }
        if (totalEntradasRegex.containsMatchIn(line)) {
            isExpense = false
            return null
        }
        
        // Tentar parsear como transação se temos uma data atual
        val date = currentDate ?: return null
        
        // Caso 1: Linha completa com descrição + valor
        transactionLineRegex.find(line)?.let { match ->
            val description = match.groupValues[1].trim()
            val valueStr = match.groupValues[2]
            
            if (shouldIgnoreDescription(description)) {
                return null
            }
            
            val amount = BrazilianPatterns.parseValueBR(valueStr) ?: return null
            val signedAmount = determineSign(description, amount)
            
            val transaction = Transaction(
                id = generateIdWithDate(fileName, startIndex, description, date.toString()),
                date = date,
                description = description,
                amount = signedAmount,
                category = Category.classify(description),
                source = dataSource,
                rawData = line
            )
            
            return ParseLineResult(transaction, startIndex + 1)
        }
        
        // Caso 2: Linha parece ser início de transação mas sem valor
        // Fazer lookahead nas próximas linhas para encontrar o valor
        if (startsWithTransactionPattern(line)) {
            // Procurar valor nas próximas 5 linhas
            for (offset in 1..5) {
                val nextIdx = startIndex + offset
                if (nextIdx >= lines.size) break
                
                val nextLine = lines[nextIdx].trim()
                
                // Se encontrar outra data, parar a busca
                if (dateTextRegex.containsMatchIn(nextLine)) break
                
                // Se a linha é apenas um valor
                valueOnlyRegex.find(nextLine)?.let { match ->
                    val valueStr = match.groupValues[1]
                    
                    if (shouldIgnoreDescription(line)) {
                        return null
                    }
                    
                    val amount = BrazilianPatterns.parseValueBR(valueStr) ?: return null
                    val signedAmount = determineSign(line, amount)
                    
                    val transaction = Transaction(
                        id = generateIdWithDate(fileName, startIndex, line, date.toString()),
                        date = date,
                        description = line,
                        amount = signedAmount,
                        category = Category.classify(line),
                        source = dataSource,
                        rawData = "$line | $nextLine"
                    )
                    
                    // Pular até a linha do valor (inclusive)
                    return ParseLineResult(transaction, nextIdx + 1)
                }
            }
        }
        
        return null
    }
    
    /** Verifica se a linha começa com um padrão conhecido de transação */
    private fun startsWithTransactionPattern(line: String): Boolean {
        val lower = line.lowercase()
        return transactionStartPatterns.any { lower.startsWith(it) }
    }
    
    /** Determina o sinal do valor baseado no tipo de transação */
    private fun determineSign(description: String, amount: Double): Double {
        return when {
            // Sempre saída (negativo): compras, transferências enviadas, pagamentos, aplicações
            isAlwaysExpense(description) -> -kotlin.math.abs(amount)
            // Sempre entrada (positivo): resgates, transferências recebidas, reembolsos
            isAlwaysIncome(description) -> kotlin.math.abs(amount)
            // Usar contexto da seção (Total de saídas/entradas)
            isExpense -> -kotlin.math.abs(amount)
            else -> kotlin.math.abs(amount)
        }
    }

    /** Transações que são SEMPRE saídas (negativas) */
    private fun isAlwaysExpense(description: String): Boolean {
        val lower = description.lowercase()
        return lower.contains("compra no débito") ||
               lower.contains("transferência enviada") ||
               lower.contains("pix enviado") ||
               lower.contains("aplicação rdb") ||
               lower.contains("pagamento de boleto")
    }
    
    /** Transações que são SEMPRE entradas (positivas) */
    private fun isAlwaysIncome(description: String): Boolean {
        val lower = description.lowercase()
        return lower.contains("resgate rdb") ||
               lower.contains("transferência recebida") ||
               lower.contains("pix recebido") ||
               lower.contains("reembolso")
    }
    
    private fun shouldIgnoreLine(line: String): Boolean {
        val lower = line.lowercase()
        return lower.contains("tem alguma dúvida") ||
               lower.contains("mande uma mensagem") ||
               lower.contains("ouvidoria") ||
               lower.contains("extrato gerado") ||
               lower.contains("saldo inicial") ||
               lower.contains("saldo final") ||
               lower.contains("rendimento líquido") ||
               lower.contains("valores em r$") ||
               lower.startsWith("cpf") ||
               lower.startsWith("agência") ||
               line.matches(Regex("""^\d{8}-\d$""")) || // Conta: 71548020-0
               line.matches(Regex("""^\d+ de \d+$""")) // Página: 1 de 6
    }
    
    private fun shouldIgnoreDescription(description: String): Boolean {
        val lower = description.lowercase()
        return lower == "movimentações" ||
               lower.startsWith("total de") ||
               lower.contains("saldo") ||
               lower.contains("pagamento de fatura") || // Ignorar - é consolidado do cartão de crédito
               description.length < 3
    }
    
    override fun supports(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return lowerName.endsWith(".pdf") && 
               (lowerName.contains("nubank") || lowerName.contains("nu_"))
    }
    
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        if (!fileName.lowercase().endsWith(".pdf")) return 0f
        
        var confidence = 0f
        val contentLower = contentPreview.lowercase()
        val fileNameLower = fileName.lowercase()
        
        if (contentLower.contains("nubank") || contentLower.contains("nu pagamentos")) {
            confidence += 0.5f
        }
        if (fileNameLower.contains("nubank") || fileNameLower.contains("nu_")) {
            confidence += 0.3f
        }
        if (contentLower.contains("roxinho") || contentLower.contains("conta digital")) {
            confidence += 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}
