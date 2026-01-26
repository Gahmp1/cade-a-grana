package com.financehub.cli

import com.financehub.cli.commands.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/**
 * Main entry point for the FinanceHub CLI (fhub)
 */
class FhubCommand : CliktCommand(
    name = "fhub",
    help = """
        FinanceHub CLI - Process bank statements and manage your finances from the terminal.
        
        Use --help on any command for more information.
    """.trimIndent(),
    printHelpOnEmptyArgs = true
) {
    override fun run() = Unit
}

fun main(args: Array<String>) = FhubCommand()
    .subcommands(
        ParseCommand(),
        ReportCommand(),
        ExportCommand(),
        VersionCommand()
    )
    .main(args)
