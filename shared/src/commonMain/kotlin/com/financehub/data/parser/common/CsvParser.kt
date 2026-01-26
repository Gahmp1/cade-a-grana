package com.financehub.data.parser.common

import com.financehub.domain.model.Transaction

/**
 * Parser especializado para arquivos CSV.
 * Fornece utilitários comuns para parsing de arquivos delimitados.
 */
abstract class CsvParser : BaseParser() {
    
    override val supportedExtensions: List<String> = listOf("csv")
    
    /**
     * Delimitador de colunas (padrão: vírgula)
     */
    open val delimiter: String = ","
    
    /**
     * Se o arquivo tem header na primeira linha
     */
    open val hasHeader: Boolean = true
    
    /**
     * Número mínimo de colunas esperado
     */
    open val minColumns: Int = 3
    
    /**
     * Índices das colunas (customizável por subclasse)
     */
    open val dateColumnIndex: Int = 0
    open val descriptionColumnIndex: Int = 1
    open val amountColumnIndex: Int = 2
    
    /**
     * Template method: implementa o parsing de CSV genérico
     */
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = fileContent.lines()
        
        lines.forEachIndexed { index, line ->
            // Pular linhas vazias
            if (line.isBlank()) return@forEachIndexed
            
            // Pular header se configurado
            if (hasHeader && index == 0) return@forEachIndexed
            
            // Fazer split e validar número de colunas
            val columns = splitLine(line)
            if (columns.size < minColumns) return@forEachIndexed
            
            try {
                val transaction = parseRow(columns, fileName, index, line)
                if (transaction != null) {
                    transactions.add(transaction)
                }
            } catch (e: Exception) {
                onParseError(line, index, e)
            }
        }
        
        return transactions
    }
    
    /**
     * Faz o split da linha respeitando o delimitador.
     * Pode ser sobrescrito para tratar casos especiais (ex: valores entre aspas)
     */
    protected open fun splitLine(line: String): List<String> {
        return line.split(delimiter).map { it.trim() }
    }
    
    /**
     * Parseia uma linha individual. Deve ser implementado por subclasses.
     * 
     * @param columns Colunas já separadas
     * @param fileName Nome do arquivo
     * @param lineIndex Índice da linha no arquivo
     * @param rawLine Linha original sem processamento
     * @return Transaction ou null se a linha deve ser ignorada
     */
    abstract fun parseRow(
        columns: List<String>,
        fileName: String,
        lineIndex: Int,
        rawLine: String
    ): Transaction?
    
    /**
     * Chamado quando ocorre erro no parsing de uma linha.
     * Pode ser sobrescrito para logging customizado.
     */
    protected open fun onParseError(line: String, lineIndex: Int, error: Exception) {
        // Por padrão, silenciosamente ignora erros
    }
    
    /**
     * Detecta o header esperado para este parser
     */
    override fun detectConfidence(fileName: String, contentPreview: String): Float {
        val extension = fileName.substringAfterLast(".").lowercase()
        if (extension != "csv") return 0f
        
        val lines = contentPreview.lines()
        val firstLine = lines.firstOrNull()?.lowercase() ?: return 0f
        
        var confidence = 0f
        
        // Verificar header patterns
        if (headerPatterns.isNotEmpty()) {
            val matchedPatterns = headerPatterns.count { pattern ->
                firstLine.contains(pattern.lowercase())
            }
            confidence += (matchedPatterns.toFloat() / headerPatterns.size) * 0.5f
        }
        
        // Verificar keywords no conteúdo
        if (contentKeywords.isNotEmpty()) {
            val contentLower = contentPreview.lowercase()
            val matchedKeywords = contentKeywords.count { keyword ->
                contentLower.contains(keyword.lowercase())
            }
            confidence += (matchedKeywords.toFloat() / contentKeywords.size) * 0.3f
        }
        
        // Verificar keywords no nome do arquivo
        val fileNameLower = fileName.lowercase()
        if (contentKeywords.any { fileNameLower.contains(it.lowercase()) }) {
            confidence += 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
}

/**
 * Parser especializado para arquivos de texto delimitados (não CSV padrão).
 * Usado para extratos bancários em formato TXT com delimitador diferente.
 */
abstract class DelimitedTextParser : CsvParser() {
    
    override val supportedExtensions: List<String> = listOf("txt", "csv")
    
    /**
     * Para arquivos TXT, geralmente não há header
     */
    override val hasHeader: Boolean = false
}
