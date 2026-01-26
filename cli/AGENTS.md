# FinanceHub CLI Agent

Agente especializado no módulo **CLI (fhub)** do FinanceHub, usando [Clikt](https://ajalt.github.io/clikt/) para interface de linha de comando.

## CLI Overview

O `fhub` CLI permite processar extratos bancários, gerar relatórios e exportar dados via terminal.

```bash
# Build CLI
./gradlew :cli:shadowJar

# Run CLI
java -jar cli/build/libs/fhub-1.0.0.jar --help
```

## Available Commands

```
fhub
├── parse     # Parse bank statement files
├── report    # Generate financial reports
├── export    # Export data to JSON
└── version   # Show version info
```

## Command Structure

### Main Entry Point

```kotlin
// cli/src/main/kotlin/com/financehub/cli/Main.kt
fun main(args: Array<String>) = FhubCommand()
    .subcommands(
        ParseCommand(),
        ReportCommand(),
        ExportCommand(),
        VersionCommand()
    )
    .main(args)
```

### Command Pattern

```kotlin
class ParseCommand : CliktCommand(
    name = "parse",
    help = "Parse bank statement files and show transactions"
) {
    // Options
    private val files by argument(help = "Files to parse")
        .file(mustExist = true)
        .multiple(required = true)
    
    private val format by option("-f", "--format", help = "Output format")
        .choice("table", "json", "csv")
        .default("table")
    
    private val output by option("-o", "--output", help = "Output file")
        .file()
    
    override fun run() {
        // Implementation
    }
}
```

## Clikt Guidelines

### Options vs Arguments

| Type | Usage | Example |
|------|-------|---------|
| **Argument** | Required positional inputs | `fhub parse file1.csv file2.csv` |
| **Option** | Optional flags with values | `fhub parse --format json file.csv` |
| **Flag** | Boolean switches | `fhub parse --verbose file.csv` |

### Defining Options

```kotlin
// Simple option with default
private val format by option("-f", "--format")
    .default("table")

// Required option
private val output by option("-o", "--output")
    .required()

// Choice option
private val type by option("--type")
    .choice("credit", "debit", "all")
    .default("all")

// Flag (boolean)
private val verbose by option("-v", "--verbose")
    .flag(default = false)

// Multiple values
private val categories by option("-c", "--category")
    .multiple()

// File option
private val inputFile by option("-i", "--input")
    .file(mustExist = true, canBeDir = false)
```

### Defining Arguments

```kotlin
// Single required argument
private val file by argument(help = "Input file")
    .file(mustExist = true)

// Multiple arguments
private val files by argument(help = "Files to process")
    .file(mustExist = true)
    .multiple(required = true)

// Optional argument
private val output by argument(help = "Output file")
    .file()
    .optional()
```

### Output Formatting

```kotlin
// Styled output
echo("Processing ${files.size} files...")
echo(style("✅ Success!", fg = green, bold = true))
echo(style("❌ Error!", fg = red))
echo(style("⚠️ Warning", fg = yellow))

// Progress
files.forEachIndexed { index, file ->
    echo("[${ index + 1 }/${files.size}] Processing ${file.name}")
}

// Table output
echo("""
    | Date       | Description          | Amount     |
    |------------|---------------------|------------|
    | 15/01/2025 | RESTAURANTE XYZ     | -R$ 50,00  |
""".trimIndent())
```

### Error Handling

```kotlin
override fun run() {
    try {
        processFiles()
    } catch (e: FileNotFoundException) {
        echo(style("Error: File not found: ${e.message}", fg = red), err = true)
        throw Abort()
    } catch (e: Exception) {
        echo(style("Error: ${e.message}", fg = red), err = true)
        throw Abort(1)
    }
}
```

## Adding New Commands

### Step 1: Create Command Class

```kotlin
// cli/src/main/kotlin/com/financehub/cli/commands/MyNewCommand.kt
package com.financehub.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*

class MyNewCommand : CliktCommand(
    name = "mycommand",
    help = "Description of what this command does"
) {
    private val input by argument(help = "Input file")
        .file(mustExist = true)
    
    private val verbose by option("-v", "--verbose")
        .flag(default = false)
    
    override fun run() {
        if (verbose) {
            echo("Verbose mode enabled")
        }
        
        // Use shared module classes
        val parser = ItauCreditCardParser()
        val content = input.readText()
        
        runBlocking {
            val transactions = parser.parse(content, input.name)
            transactions.forEach { tx ->
                echo("${tx.date} | ${tx.description} | ${tx.amount}")
            }
        }
    }
}
```

### Step 2: Register in Main

```kotlin
// cli/src/main/kotlin/com/financehub/cli/Main.kt
fun main(args: Array<String>) = FhubCommand()
    .subcommands(
        ParseCommand(),
        ReportCommand(),
        ExportCommand(),
        VersionCommand(),
        MyNewCommand()  // Add here
    )
    .main(args)
```

## Usage Examples

### Parse Files

```bash
# Parse single file
fhub parse Itau-fatura-20250106.csv

# Parse multiple files
fhub parse *.csv

# Parse with JSON output
fhub parse --format json file.csv > transactions.json

# Parse to output file
fhub parse -o output.json --format json file.csv
```

### Generate Report

```bash
# Monthly report
fhub report --month 2025-01 file.csv

# Multiple months
fhub report --from 2025-01 --to 2025-06 *.csv

# Report to file
fhub report -o relatorio.txt --month 2025-01 file.csv
```

### Export Data

```bash
# Export all transactions to JSON
fhub export --format json -o transactions.json *.csv

# Export filtered by category
fhub export --category ALIMENTACAO --category TRANSPORTE -o filtered.json file.csv

# Export with date range
fhub export --from 2025-01-01 --to 2025-01-31 -o january.json file.csv
```

## Project Structure

```
cli/
├── build.gradle.kts          # Clikt + Shadow plugin config
├── AGENTS.md                 # This file
└── src/
    └── main/
        └── kotlin/
            └── com/
                └── financehub/
                    └── cli/
                        ├── Main.kt           # Entry point + FhubCommand
                        └── commands/
                            ├── ParseCommand.kt
                            ├── ReportCommand.kt
                            ├── ExportCommand.kt
                            └── VersionCommand.kt
```

## Integration with Shared Module

The CLI module has access to all shared business logic:

```kotlin
import com.financehub.data.parser.ItauCreditCardParser
import com.financehub.data.repository.TransactionRepositoryImpl
import com.financehub.domain.model.Transaction
import com.financehub.domain.model.Category
import com.financehub.domain.usecase.ProcessFilesUseCase
```

## Testing Commands

```bash
# After building
./gradlew :cli:shadowJar

# Test version
java -jar cli/build/libs/fhub-1.0.0.jar version

# Test help
java -jar cli/build/libs/fhub-1.0.0.jar --help
java -jar cli/build/libs/fhub-1.0.0.jar parse --help

# Test with sample files
java -jar cli/build/libs/fhub-1.0.0.jar parse Notas/Itau-fatura-20250106.csv
```

## Embedding in DMG

The CLI JAR is automatically bundled into the macOS app:

```
FinanceHub.app/
└── Contents/
    └── Resources/
        └── fhub-cli.jar
```

Users can run it via:
```bash
java -jar "/Applications/FinanceHub.app/Contents/Resources/fhub-cli.jar" --help
```

---

**For Clikt documentation, see: https://ajalt.github.io/clikt/**
