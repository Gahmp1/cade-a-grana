package com.financehub.data.parser

import com.financehub.domain.model.Category
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction
import kotlinx.datetime.LocalDate
import java.io.File

/**
 * Parser Desktop para PDF do PicPay usando Apache PDFBox
 */
class PicPayPdfParser : FileParser {
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        // fileContent neste caso é o caminho do arquivo
        return parsePdfFile(fileContent, fileName)
    }
    
    fun parsePdfFile(filePath: String, fileName: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        try {
            // TODO: Integrar PDFBox quando estiver configurado
            // Por enquanto, retorna lista vazia
            // val document = org.apache.pdfbox.pdmodel.PDDocument.load(File(filePath))
            // val stripper = org.apache.pdfbox.text.PDFTextStripper()
            // val text = stripper.getText(document)
            // document.close()
            
            val text = ""
            
            // Processar texto extraído
            val lines = text.lines()
            var index = 0
            
            lines.forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) return@forEach
                
                // Padrão PicPay: procurar por padrões de data e valor
                // Exemplo: "05/08/2025 Transferência R$ 100,00"
                val dateRegex = """(\d{2}/\d{2}/\d{4})""".toRegex()
                val valueRegex = """R\$\s*(\d+\.?\d*,\d{2})""".toRegex()
                
                val dateMatch = dateRegex.find(trimmedLine)
                val valueMatch = valueRegex.find(trimmedLine)
                
                if (dateMatch != null && valueMatch != null) {
                    try {
                        val dateStr = dateMatch.groupValues[1]
                        val valueStr = valueMatch.groupValues[1]
                            .replace(".", "")
                            .replace(",", ".")
                        
                        val date = parseDate(dateStr)
                        val rawAmount = valueStr.toDoubleOrNull() ?: 0.0
                        
                        // Extrair descrição (texto entre data e valor)
                        val description = trimmedLine
                            .substring(dateMatch.range.last + 1, valueMatch.range.first)
                            .trim()
                        
                        // Determinar tipo baseado em keywords
                        val amount = when {
                            trimmedLine.lowercase().contains("recebido") ||
                            trimmedLine.lowercase().contains("entrada") ||
                            trimmedLine.lowercase().contains("crédito") -> rawAmount
                            
                            else -> -rawAmount
                        }
                        
                        val category = Category.classify(description)
                        
                        transactions.add(
                            Transaction(
                                id = generateId(fileName, index, description),
                                date = date,
                                description = description,
                                amount = amount,
                                category = category,
                                source = DataSource.PICPAY_STATEMENT,
                                rawData = trimmedLine
                            )
                        )
                        
                        index++
                    } catch (e: Exception) {
                        println("Error parsing PDF line: ${e.message}")
                    }
                }
            }
            
        } catch (e: Exception) {
            println("Error loading PDF: ${e.message}")
        }
        
        return transactions
    }
    
    override fun supports(fileName: String): Boolean {
        return fileName.lowercase().contains("picpay") && 
               fileName.endsWith(".pdf")
    }
    
    private fun parseDate(dateStr: String): LocalDate {
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
