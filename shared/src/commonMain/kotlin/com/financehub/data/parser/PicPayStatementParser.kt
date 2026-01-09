package com.financehub.data.parser

import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction
import kotlinx.datetime.LocalDate

/**
 * Parser para extrato PicPay (PDF)
 * Processa texto extraído de PDF do PicPay buscando por padrões de data e valor
 */
class PicPayStatementParser : FileParser {
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = fileContent.lines()
        
        println("🔍 PicPayStatementParser: Iniciando parsing")
        println("   Arquivo: $fileName")
        println("   Total de linhas: ${lines.size}")
        println("   Primeiras 30 linhas:")
        lines.take(30).forEachIndexed { i, line ->
            println("   $i: ${line.take(100)}${if (line.length > 100) "..." else ""}")
        }
        
        // Padrões para identificar transações no PDF extraído
        val dateRegex = """(\d{2}/\d{2}/\d{4})""".toRegex()
        val valueRegex = """R\$\s*(\d{1,3}(?:\.\d{3})*,\d{2})""".toRegex()
        
        var skippedLines = 0
        var attemptedParsing = 0
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            
            if (line.isBlank() || line.length < 10) {
                skippedLines++
                i++
                continue
            }
            
            try {
                // Verificar se a linha tem apenas data (dd/MM/yyyy)
                val dateLine = line.trim()
                if (dateLine.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                    // Próxima linha deve ter a transação
                    if (i + 1 < lines.size) {
                        val nextLine = lines[i + 1]
                        val valueMatch = valueRegex.findAll(nextLine).firstOrNull() // Primeiro valor = transação
                        
                        if (valueMatch != null) {
                            attemptedParsing++
                            
                            if (attemptedParsing <= 10) {
                                println("   ✓ Transação $attemptedParsing: data=$dateLine")
                                println("      ${nextLine.take(80)}")
                            }
                            
                            val dateStr = dateLine
                            val valueStr = valueMatch.groupValues[1]
                                .replace(".", "")  // Remove separador de milhar
                                .replace(",", ".")  // Converte vírgula decimal para ponto
                            
                            // Descrição é o texto antes do valor
                            val valueStartIndex = valueMatch.range.first
                            var description = nextLine.substring(0, valueStartIndex).trim()
                            
                            // Remover hora (HH:MM:SS) do início da descrição
                            description = description.replace(Regex("^\\d{2}:\\d{2}:\\d{2}\\s*"), "").trim()
                            
                            // Remover "R$" se sobrou
                            description = description.replace("R$", "").trim()
                            
                            if (description.length >= 3) {
                                val date = parseDate(dateStr)
                                val rawAmount = valueStr.toDoubleOrNull()
                                
                                if (rawAmount != null && rawAmount > 0) {
                                    // Determinar se é entrada ou saída por palavras-chave
                                    val isIncome = description.lowercase().let { desc ->
                                        desc.contains("recebido") || 
                                        desc.contains("recebeu") ||
                                        desc.contains("recebimento") ||
                                        desc.contains("entrada") ||
                                        desc.contains("crédito") ||
                                        desc.contains("depósito") ||
                                        desc.contains("pix recebido") ||
                                        desc.contains("transferência recebida")
                                    }
                                    
                                    val amount = if (isIncome) rawAmount else -rawAmount
                                    val category = Category.classify(description)
                                    
                                    transactions.add(
                                        Transaction(
                                            id = generateId(fileName, i, description),
                                            date = date,
                                            description = description,
                                            amount = amount,
                                            category = category,
                                            source = DataSource.PICPAY_STATEMENT,
                                            rawData = "$dateLine | $nextLine"
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Pular a próxima linha pois já processamos
                        i += 2
                        continue
                    }
                }
            } catch (e: Exception) {
                // Silenciosamente ignorar linhas com erro
            }
            
            i++
        }
        
        println("\n📊 Estatísticas do parsing:")
        println("   Total de linhas: ${lines.size}")
        println("   Linhas ignoradas: $skippedLines")
        println("   Tentativas de parsing: $attemptedParsing")
        println("   Transações encontradas: ${transactions.size}")
        
        return transactions
    }
    
    override fun supports(fileName: String): Boolean {
        return fileName.lowercase().contains("picpay") && 
               fileName.endsWith(".pdf")
    }
    
    private fun parseDate(dateStr: String): LocalDate {
        // Formato: dd/MM/yyyy
        val parts = dateStr.split("/")
        return LocalDate(
            year = parts[2].toInt(),
            monthNumber = parts[1].toInt(),
            dayOfMonth = parts[0].toInt()
        )
    }
    
    private fun generateId(fileName: String, index: Int, description: String): String {
        return "${fileName}_${index}_${description.hashCode()}"
    }
}
