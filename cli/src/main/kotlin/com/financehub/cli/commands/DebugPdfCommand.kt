package com.financehub.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

/**
 * Debug command to extract and display raw text from PDF files
 */
class DebugPdfCommand : CliktCommand(
    name = "debug-pdf",
    help = "Extract and display raw text from a PDF file"
) {
    private val file by argument(help = "PDF file to debug")
        .file(mustExist = true, canBeDir = false)

    override fun run() {
        println("📄 Extracting text from: ${file.name}")
        println("=" .repeat(60))
        
        try {
            val document = Loader.loadPDF(file)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            
            val lines = text.lines()
            println("Total lines: ${lines.size}")
            println("=" .repeat(60))
            
            lines.forEachIndexed { index, line ->
                println("${(index + 1).toString().padStart(3)}: $line")
            }
            
            println("=" .repeat(60))
            println("✅ Done")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
        }
    }
}
