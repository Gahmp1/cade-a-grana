package com.financehub.data.parser.common

import com.financehub.domain.model.Transaction

/**
 * Parser especializado para arquivos PDF.
 * O conteúdo recebido já é texto extraído do PDF.
 */
abstract class PdfParser : BaseParser() {
    
    override val supportedExtensions: List<String> = listOf("pdf")
    
    /**
     * Template method: implementa o parsing de PDF genérico
     * O fileContent já é o texto extraído do PDF
     */
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = fileContent.lines()
        
        // Log para debug
        if (enableDebugLogs) {
            println("🔍 ${this::class.simpleName}: Iniciando parsing")
            println("   Arquivo: $fileName")
            println("   Total de linhas: ${lines.size}")
        }
        
        var stats = ParsingStats()
        
        var lineIndex = 0
        while (lineIndex < lines.size) {
            val line = lines[lineIndex]
            
            // Pular linhas vazias ou muito curtas
            if (line.isBlank() || line.length < minLineLength) {
                stats.skippedLines++
                lineIndex++
                continue
            }
            
            try {
                // Tentar parsing da linha (pode consumir múltiplas linhas)
                val result = parseLines(lines, lineIndex, fileName)
                
                if (result != null) {
                    transactions.add(result.transaction)
                    stats.successfulParsing++
                    lineIndex = result.nextLineIndex
                    
                    if (enableDebugLogs && stats.successfulParsing <= 10) {
                        println("   ✓ Transação ${stats.successfulParsing}: ${result.transaction.description.take(50)}")
                    }
                } else {
                    stats.failedParsing++
                    lineIndex++
                }
            } catch (e: Exception) {
                stats.errors++
                onParseError(line, lineIndex, e)
                lineIndex++
            }
        }
        
        if (enableDebugLogs) {
            println("\n📊 Estatísticas do parsing:")
            println("   Total de linhas: ${lines.size}")
            println("   Linhas ignoradas: ${stats.skippedLines}")
            println("   Parsing com sucesso: ${stats.successfulParsing}")
            println("   Parsing falhou: ${stats.failedParsing}")
            println("   Erros: ${stats.errors}")
        }
        
        return transactions
    }
    
    /**
     * Parseia uma ou mais linhas do PDF.
     * 
     * @param lines Todas as linhas do documento
     * @param startIndex Índice da linha atual
     * @param fileName Nome do arquivo
     * @return ParseLineResult com transação e próximo índice, ou null se não é transação
     */
    abstract fun parseLines(
        lines: List<String>,
        startIndex: Int,
        fileName: String
    ): ParseLineResult?
    
    /**
     * Comprimento mínimo de linha para tentar parsing
     */
    open val minLineLength: Int = 10
    
    /**
     * Habilita logs de debug durante parsing
     */
    open val enableDebugLogs: Boolean = true
    
    /**
     * Chamado quando ocorre erro no parsing de uma linha
     */
    protected open fun onParseError(line: String, lineIndex: Int, error: Exception) {
        if (enableDebugLogs) {
            println("   ⚠️ Erro linha $lineIndex: ${error.message}")
        }
    }
    
    /**
     * Detecta confiança baseada no conteúdo do PDF
     */
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        val extension = fileName.substringAfterLast(".").lowercase()
        if (extension != "pdf") return 0f
        
        val contentLower = contentPreview.lowercase()
        var confidence = 0f
        
        // Verificar keywords no conteúdo
        if (contentKeywords.isNotEmpty()) {
            val matchedKeywords = contentKeywords.count { keyword ->
                contentLower.contains(keyword.lowercase())
            }
            confidence += (matchedKeywords.toFloat() / contentKeywords.size) * 0.7f
        }
        
        // Verificar keywords no nome do arquivo
        val fileNameLower = fileName.lowercase()
        if (contentKeywords.any { fileNameLower.contains(it.lowercase()) }) {
            confidence += 0.3f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
    
    /**
     * Resultado do parsing de uma linha
     */
    data class ParseLineResult(
        val transaction: Transaction,
        val nextLineIndex: Int
    )
    
    /**
     * Estatísticas de parsing
     */
    data class ParsingStats(
        var skippedLines: Int = 0,
        var successfulParsing: Int = 0,
        var failedParsing: Int = 0,
        var errors: Int = 0
    )
}
