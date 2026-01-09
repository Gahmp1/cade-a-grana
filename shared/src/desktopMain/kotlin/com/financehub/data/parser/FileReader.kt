package com.financehub.data.parser

import java.io.File

/**
 * Implementação Desktop do FileReader
 */
actual class FileReader {
    
    actual suspend fun readFile(filePath: String): String {
        val file = File(filePath)
        
        // Se for PDF, usar PDFBox para extrair texto
        if (filePath.lowercase().endsWith(".pdf")) {
            return try {
                println("📄 Extraindo texto do PDF: ${file.name}")
                val document = org.apache.pdfbox.Loader.loadPDF(file)
                val stripper = org.apache.pdfbox.text.PDFTextStripper()
                val text = stripper.getText(document)
                document.close()
                println("   ✓ Extraídos ${text.lines().size} linhas do PDF")
                text
            } catch (e: Exception) {
                println("   ❌ Erro ao extrair PDF: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
        
        // Caso contrário, ler como texto normal
        return file.readText(Charsets.UTF_8)
    }
    
    actual suspend fun listFiles(directoryPath: String): List<String> {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }
        
        return directory.listFiles()
            ?.filter { it.isFile }
            ?.map { it.absolutePath }
            ?: emptyList()
    }
    
    actual suspend fun writeFile(filePath: String, content: String) {
        File(filePath).writeText(content, Charsets.UTF_8)
    }
}
