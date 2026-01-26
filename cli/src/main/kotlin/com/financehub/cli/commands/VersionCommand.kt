package com.financehub.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold

/**
 * Display version and build information
 */
class VersionCommand : CliktCommand(
    name = "version",
    help = "Show version and build information"
) {
    companion object {
        const val VERSION = "1.0.0"
        const val BUILD_DATE = "2026-01-25"
        const val KOTLIN_VERSION = "2.0.0"
    }

    override fun run() {
        echo((bold + cyan)("FinanceHub CLI (fhub)"))
        echo("")
        echo("  Version:     $VERSION")
        echo("  Build Date:  $BUILD_DATE")
        echo("  Kotlin:      $KOTLIN_VERSION")
        echo("")
        echo((white)("Supported formats:"))
        echo("  • Itaú Credit Card (CSV)")
        echo("  • Itaú Bank Statement (TXT)")
        echo("  • PicPay Statement (PDF) - Desktop only")
        echo("")
        echo((gray)("For more info: fhub --help"))
        echo((gray)("Repository: https://github.com/yourusername/FinanceHubKMP"))
    }
}
