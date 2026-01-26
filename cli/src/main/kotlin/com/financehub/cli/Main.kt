package com.financehub.cli

import com.financehub.cli.commands.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/**
 * Main entry point for the Cadê a Grana? CLI (grana)
 */
class GranaCommand : CliktCommand(
    name = "grana",
    help = """
        Cadê a Grana? CLI - Processe extratos bancários e gerencie suas finanças pelo terminal.
        
        Use --help em qualquer comando para mais informações.
    """.trimIndent(),
    printHelpOnEmptyArgs = true
) {
    override fun run() = Unit
}

fun main(args: Array<String>) = GranaCommand()
    .subcommands(
        ParseCommand(),
        ReportCommand(),
        ExportCommand(),
        VersionCommand(),
        DebugPdfCommand()
    )
    .main(args)
