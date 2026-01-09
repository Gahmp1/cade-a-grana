package com.financehub.data.parser

import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction
import kotlinx.datetime.LocalDate

/**
 * Parser para extrato bancário Itaú (TXT)
 * Formato esperado: data;descricao;valor
 */
class ItauBankStatementParser : FileParser {
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = fileContent.lines()
        
        lines.forEachIndexed { index, line ->
            if (line.isBlank()) return@forEachIndexed
            
            // Pular header se existir
            if (index == 0 && line.lowercase().contains("data")) return@forEachIndexed
            
            try {
                val parts = line.split(";")
                
                // Validar que temos pelo menos 3 campos
                if (parts.size < 3) {
                    return@forEachIndexed
                }
                
                val dateStr = parts[0].trim()
                val description = parts[1].trim()
                val amountStr = parts[2].trim()
                    .replace("R$", "")
                    .replace(".", "")  // Remove separador de milhar
                    .replace(",", ".")  // Troca vírgula por ponto decimal
                    .trim()
                
                // Validar campos não vazios
                if (dateStr.isEmpty() || description.isEmpty() || amountStr.isEmpty()) {
                    return@forEachIndexed
                }
                
                // Validar formato de data básico (dd/MM/yyyy)
                if (!dateStr.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                    return@forEachIndexed
                }
                
                val date = parseDate(dateStr)
                val amount = amountStr.toDoubleOrNull()
                
                // Se não conseguir converter o valor, pular linha
                if (amount == null) {
                    return@forEachIndexed
                }
                
                // No extrato bancário:
                // - Valores positivos são ENTRADAS (receita)
                // - Valores negativos são SAÍDAS (gastos)
                
                val category = Category.classify(description)
                
                transactions.add(
                    Transaction(
                        id = generateId(fileName, index, description),
                        date = date,
                        description = description,
                        amount = amount,
                        category = category,
                        source = DataSource.ITAU_BANK_STATEMENT,
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
               fileName.contains("extrato", ignoreCase = true) &&
               (fileName.endsWith(".txt") || fileName.endsWith(".csv"))
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
