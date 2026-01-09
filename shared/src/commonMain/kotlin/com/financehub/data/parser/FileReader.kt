package com.financehub.data.parser

/**
 * Interface para leitura de arquivos específica de cada plataforma
 */
expect class FileReader {
    /**
     * Lê o conteúdo de um arquivo
     */
    suspend fun readFile(filePath: String): String
    
    /**
     * Lista arquivos em um diretório
     */
    suspend fun listFiles(directoryPath: String): List<String>
    
    /**
     * Escreve conteúdo em um arquivo
     */
    suspend fun writeFile(filePath: String, content: String)
}
