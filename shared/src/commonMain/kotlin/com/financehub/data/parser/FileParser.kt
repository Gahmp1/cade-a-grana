package com.financehub.data.parser

import com.financehub.domain.model.Transaction

/**
 * Interface comum para parsers de diferentes fontes de dados
 */
interface FileParser {
    /**
     * Faz o parse de um arquivo e retorna lista de transações
     */
    suspend fun parse(fileContent: String, fileName: String): List<Transaction>
    
    /**
     * Verifica se este parser suporta o arquivo
     */
    fun supports(fileName: String): Boolean
}

/**
 * Resultado do parsing
 */
sealed class ParseResult {
    data class Success(val transactions: List<Transaction>) : ParseResult()
    data class Error(val message: String, val exception: Throwable? = null) : ParseResult()
}
