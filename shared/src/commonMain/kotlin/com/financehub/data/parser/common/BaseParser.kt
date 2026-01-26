package com.financehub.data.parser.common

import com.financehub.data.parser.FileParser
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.Transaction

/**
 * Classe base abstrata para todos os parsers de documentos financeiros.
 * Implementa funcionalidades comuns e define contrato para detecção automática.
 */
abstract class BaseParser : FileParser {
    
    /**
     * Identificador único do banco/fonte
     */
    abstract val bankIdentifier: String
    
    /**
     * Nome amigável para exibição
     */
    abstract val displayName: String
    
    /**
     * Fonte de dados associada a este parser
     */
    abstract val dataSource: DataSource
    
    /**
     * Extensões de arquivo suportadas (ex: listOf("csv", "pdf", "txt"))
     */
    abstract val supportedExtensions: List<String>
    
    // ==================== DETECÇÃO AUTOMÁTICA ====================
    
    /**
     * Detecta se o parser suporta o arquivo baseado no CONTEÚDO.
     * Substitui o antigo supports(fileName) que era baseado apenas no nome.
     * 
     * @param fileName Nome do arquivo (para tipo de extensão)
     * @param contentPreview Primeiras linhas do arquivo para análise
     * @return Confiança de 0.0 a 1.0 (0 = não suporta, 1 = certeza absoluta)
     */
    abstract fun detectConfidence(fileName: String, contentPreview: String): Float
    
    /**
     * Palavras-chave que indicam este banco no conteúdo do arquivo
     */
    open val contentKeywords: List<String> = emptyList()
    
    /**
     * Padrões de header que identificam este parser (para CSVs)
     */
    open val headerPatterns: List<String> = emptyList()
    
    /**
     * Implementação padrão do supports baseada em extensão + conteúdo.
     * Subclasses podem sobrescrever para lógica customizada.
     */
    override fun supports(fileName: String): Boolean {
        // Verificar extensão
        val extension = fileName.substringAfterLast(".").lowercase()
        if (extension !in supportedExtensions) return false
        
        // Se não temos keywords, aceitar só pela extensão
        if (contentKeywords.isEmpty()) return true
        
        // Verificar nome do arquivo contém alguma keyword
        val lowerName = fileName.lowercase()
        return contentKeywords.any { keyword -> 
            lowerName.contains(keyword.lowercase()) 
        }
    }
    
    // ==================== GERAÇÃO DE ID ====================
    
    /**
     * Gera um ID único para a transação baseado no arquivo, índice e descrição
     */
    protected fun generateId(fileName: String, index: Int, description: String): String {
        val descHash = description.hashCode().let { if (it < 0) it * -1 else it }
        return "${bankIdentifier}_${fileName.substringBefore(".")}_${index}_$descHash"
    }
    
    /**
     * Gera ID mais robusto incluindo data para evitar colisões
     */
    protected fun generateIdWithDate(
        fileName: String, 
        index: Int, 
        description: String,
        dateStr: String
    ): String {
        val descHash = description.hashCode().let { if (it < 0) it * -1 else it }
        val dateHash = dateStr.replace("/", "").replace("-", "")
        return "${bankIdentifier}_${dateHash}_${index}_$descHash"
    }
    
    // ==================== UTILIDADES DE PARSING ====================
    
    /**
     * Verifica se a linha deve ser ignorada (headers, linhas vazias, etc.)
     */
    protected fun shouldSkipLine(line: String, lineIndex: Int): Boolean {
        if (line.isBlank()) return true
        if (line.length < 5) return true
        
        // Headers comuns
        val lowerLine = line.lowercase()
        if (lineIndex == 0 && (
            lowerLine.contains("data") ||
            lowerLine.contains("date") ||
            lowerLine.contains("descrição") ||
            lowerLine.contains("description") ||
            lowerLine.contains("valor") ||
            lowerLine.contains("amount")
        )) return true
        
        return false
    }
    
    /**
     * Detecta se uma transação é entrada (receita) ou saída (gasto)
     * baseado em palavras-chave na descrição
     */
    protected fun isIncomeTransaction(description: String): Boolean {
        val lowerDesc = description.lowercase()
        return INCOME_KEYWORDS.any { keyword -> lowerDesc.contains(keyword) }
    }
    
    /**
     * Detecta se uma transação é transferência entre contas
     */
    protected fun isTransferTransaction(description: String): Boolean {
        val lowerDesc = description.lowercase()
        return TRANSFER_KEYWORDS.any { keyword -> lowerDesc.contains(keyword) }
    }
    
    companion object {
        /**
         * Palavras-chave que indicam entrada de dinheiro
         */
        val INCOME_KEYWORDS = listOf(
            "recebido", "recebeu", "recebimento",
            "entrada", "crédito", "credito",
            "depósito", "deposito",
            "pix recebido", "transferência recebida", "transferencia recebida",
            "salário", "salario",
            "rendimento", "rend pago",
            "cashback", "estorno",
            "reembolso"
        )
        
        /**
         * Palavras-chave que indicam transferência entre contas
         */
        val TRANSFER_KEYWORDS = listOf(
            "transferência", "transferencia",
            "pix enviado", "pix recebido",
            "ted", "doc",
            "entre contas"
        )
        
        /**
         * Descrições que devem ser ignoradas (não são transações reais)
         */
        val IGNORE_DESCRIPTIONS = listOf(
            "saldo anterior", "saldo inicial", "saldo final",
            "total", "subtotal",
            "pagamento efetuado", "pagamento recebido"
        )
    }
}
