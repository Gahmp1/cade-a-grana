package com.financehub.data.parser

import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction
import kotlinx.datetime.LocalDate

/**
 * Parser para arquivos CSV do cartão Itaú
 * Formato esperado: data,descricao,valor
 */
class ItauCreditCardParser : FileParser {
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = fileContent.lines()
        
        lines.forEachIndexed { index, line ->
            if (line.isBlank()) return@forEachIndexed
            
            // Pular header se existir
            if (index == 0 && line.lowercase().contains("data")) return@forEachIndexed
            
            try {
                val parts = line.split(",")
                
                // Validar que temos pelo menos 3 campos
                if (parts.size < 3) {
                    return@forEachIndexed
                }
                
                val dateStr = parts[0].trim()
                val description = parts[1].trim()
                val amountStr = parts[2].trim()
                    .replace("R$", "")
                    .trim()
                
                // Validar campos não vazios
                if (dateStr.isEmpty() || description.isEmpty() || amountStr.isEmpty()) {
                    return@forEachIndexed
                }
                
                // Validar formato de data básico (dd/MM/yyyy ou yyyy-MM-dd)
                if (!dateStr.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) && 
                    !dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    return@forEachIndexed
                }
                
                val date = parseDate(dateStr)
                val amount = amountStr.toDoubleOrNull()
                
                // Se não conseguir converter o valor, pular linha
                if (amount == null) {
                    return@forEachIndexed
                }
                
                // No cartão de crédito:
                // - Valores positivos são GASTOS (saída) -> converter para negativo
                // - Valores negativos são ESTORNOS/PAGAMENTOS (entrada) -> manter positivo
                val normalizedAmount = -amount
                
                val category = Category.classify(description)
                
                transactions.add(
                    Transaction(
                        id = generateId(fileName, index, description),
                        date = date,
                        description = description,
                        amount = normalizedAmount,
                        category = category,
                        source = DataSource.ITAU_CREDIT_CARD,
                        rawData = line
                    )
                )
            } catch (e: Exception) {
                // Silenciosamente pular linhas com erro
            }
        }
        
        return transactions
    }
    
    override fun supports(fileName: String): Boolean {
        // Ignorar relatórios gerados
        if (fileName.startsWith("relatorio")) return false
        
        return (fileName.startsWith("Itau-") || fileName.startsWith("Itau")) && 
               fileName.endsWith(".csv") &&
               !fileName.contains("extrato", ignoreCase = true)
    }
    
    private fun parseDate(dateStr: String): LocalDate {
        return try {
            // Formato: yyyy-MM-dd
            if (dateStr.contains("-")) {
                val parts = dateStr.split("-")
                LocalDate(
                    year = parts[0].toInt(),
                    monthNumber = parts[1].toInt(),
                    dayOfMonth = parts[2].toInt()
                )
            } else {
                // Formato: dd/MM/yyyy
                val parts = dateStr.split("/")
                LocalDate(
                    year = parts[2].toInt(),
                    monthNumber = parts[1].toInt(),
                    dayOfMonth = parts[0].toInt()
                )
            }
        } catch (e: Exception) {
            // Fallback para data padrão
            LocalDate(2025, 1, 1)
        }
    }
    
    private fun generateId(fileName: String, index: Int, description: String): String {
        return "${fileName}_${index}_${description.hashCode()}"
    }
}
