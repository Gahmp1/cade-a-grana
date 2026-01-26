# FinanceHub Architecture Agent

Agente especializado na **arquitetura Kotlin Multiplatform** e **sistema de Parsers** do FinanceHub.

## Architecture Overview

O módulo `shared` contém toda a lógica de negócio, compartilhada entre Desktop, Android e iOS.

```
shared/src/
├── commonMain/kotlin/com/financehub/
│   ├── domain/           # Entities, UseCases, Repository interfaces
│   │   ├── model/        # Transaction, Category, ChangeLogEntry
│   │   ├── usecase/      # Business logic (ProcessFilesUseCase, etc.)
│   │   └── repository/   # Repository interfaces
│   ├── data/             # Implementations
│   │   ├── parser/       # File parsers (Itau, PicPay, etc.)
│   │   └── repository/   # TransactionRepositoryImpl
│   └── presentation/     # MVI (ViewModel, State, Event)
├── desktopMain/          # Desktop-specific (expect/actual)
├── androidMain/          # Android-specific
└── iosMain/              # iOS-specific
```

## MVI Pattern

### Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                          UI Layer                           │
│  ┌─────────┐    ┌──────────────┐    ┌─────────────────┐    │
│  │ Compose │───▶│ FinanceEvent │───▶│ collectAsState  │    │
│  │   UI    │◀───│              │◀───│  FinanceState   │    │
│  └─────────┘    └──────────────┘    └─────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                           │ ▲
                           ▼ │
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                  FinanceViewModel                     │   │
│  │  • StateFlow<FinanceState>                           │   │
│  │  • fun onEvent(FinanceEvent)                         │   │
│  │  • Coroutine scope for async operations              │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           │ ▲
                           ▼ │
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐   │
│  │ ProcessFiles   │  │ GenerateReport │  │ ExportData   │   │
│  │    UseCase     │  │    UseCase     │  │   UseCase    │   │
│  └────────────────┘  └────────────────┘  └──────────────┘   │
│                           │                                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           TransactionRepository (interface)          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           │ ▲
                           ▼ │
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │            TransactionRepositoryImpl                  │   │
│  │  • List<FileParser> parsers                          │   │
│  │  • Delegates to appropriate parser                   │   │
│  └──────────────────────────────────────────────────────┘   │
│                           │                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐   │
│  │ItauCreditCard  │  │ItauBankStatement│  │  PicPay     │   │
│  │    Parser      │  │     Parser      │  │   Parser    │   │
│  └────────────────┘  └────────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### State Definition

```kotlin
data class FinanceState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val selectedFiles: List<String> = emptyList(),
    val error: String? = null,
    val report: MonthlyReport? = null
)
```

### Event Definition

```kotlin
sealed class FinanceEvent {
    data class FilesSelected(val files: List<File>) : FinanceEvent()
    data class ProcessFiles(val files: List<File>) : FinanceEvent()
    data class UpdateCategory(val id: String, val category: Category) : FinanceEvent()
    data class ToggleType(val id: String) : FinanceEvent()
    object ExportChangelog : FinanceEvent()
    object GenerateReport : FinanceEvent()
}
```

### ViewModel Pattern

```kotlin
class FinanceViewModel(
    private val processFilesUseCase: ProcessFilesUseCase,
    private val generateReportUseCase: GenerateReportUseCase
) {
    private val _state = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()
    
    fun onEvent(event: FinanceEvent) {
        when (event) {
            is FinanceEvent.ProcessFiles -> processFiles(event.files)
            is FinanceEvent.UpdateCategory -> updateCategory(event.id, event.category)
            // ...
        }
    }
    
    private fun processFiles(files: List<File>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            processFilesUseCase(files)
                .onSuccess { transactions ->
                    _state.update { it.copy(
                        isLoading = false,
                        transactions = transactions
                    )}
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
        }
    }
}
```

## Expect/Actual Pattern

### When to Use

Use `expect/actual` when:
- Accessing platform-specific APIs (file system, dialogs)
- Using platform-specific libraries (PDFBox on Desktop only)
- Different implementations per platform

### Example: FileReader

```kotlin
// commonMain - Interface
expect class FileReader {
    suspend fun readFile(path: String): Result<String>
    suspend fun readPdf(path: String): Result<String>
}

// desktopMain - Implementation
actual class FileReader {
    actual suspend fun readFile(path: String): Result<String> {
        return runCatching {
            File(path).readText()
        }
    }
    
    actual suspend fun readPdf(path: String): Result<String> {
        return runCatching {
            PDDocument.load(File(path)).use { doc ->
                PDFTextStripper().getText(doc)
            }
        }
    }
}

// androidMain - Implementation
actual class FileReader {
    actual suspend fun readFile(path: String): Result<String> {
        // Use Android ContentResolver
    }
    
    actual suspend fun readPdf(path: String): Result<String> {
        // Use Android PDF library or return error
    }
}
```

---

# Parser System

## Parser Interface

```kotlin
interface FileParser {
    /**
     * Check if this parser can handle the given file
     * @param fileName Name of the file (used for format detection)
     * @return true if this parser supports the file
     */
    fun supports(fileName: String): Boolean
    
    /**
     * Parse file content into transactions
     * @param fileContent Raw content of the file
     * @param fileName Original file name (for context)
     * @return List of parsed transactions
     */
    suspend fun parse(fileContent: String, fileName: String): List<Transaction>
}
```

## Implemented Parsers

### 1. ItauCreditCardParser (CSV)

**File Pattern**: `Itau-fatura-*.csv`, `fatura-itau*.csv`

**Format**:
```csv
data,lancamento,valor
06/07/2025,RESTAURANTE XYZ,77.12
06/07/2025,ESTORNO COMPRA,-50.00
```

**Rules**:
- Values use American format (dot = decimal): `77.12`
- Positive values = expenses (converted to negative)
- Negative values = refunds (kept positive)

```kotlin
class ItauCreditCardParser : FileParser {
    override fun supports(fileName: String): Boolean {
        return fileName.contains("itau", ignoreCase = true) &&
               fileName.contains("fatura", ignoreCase = true) &&
               fileName.endsWith(".csv", ignoreCase = true)
    }
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        return fileContent.lines()
            .drop(1)  // Skip header
            .filter { it.isNotBlank() }
            .mapNotNull { line -> parseLine(line) }
    }
    
    private fun parseLine(line: String): Transaction? {
        val parts = line.split(",")
        if (parts.size < 3) return null
        
        val date = parseDate(parts[0])
        val description = parts[1]
        val rawAmount = parts[2].toDoubleOrNull() ?: return null
        
        // Positive = expense (convert to negative)
        // Negative = refund (convert to positive)
        val amount = -rawAmount
        
        return Transaction(
            id = UUID.randomUUID().toString(),
            date = date,
            description = description,
            amount = amount,
            category = categorize(description),
            source = DataSource.ITAU_CREDIT_CARD,
            rawData = line
        )
    }
}
```

### 2. ItauBankStatementParser (TXT)

**File Pattern**: `Itau-Extrato*.txt`

### 3. PicPayStatementParser (PDF)

**File Pattern**: `*picpay*.pdf`

**Note**: Desktop only (uses PDFBox)

## Adding a New Parser

### Step 1: Create Parser Class

```kotlin
// shared/src/commonMain/kotlin/com/financehub/data/parser/NubankParser.kt
class NubankCreditCardParser : FileParser {
    
    override fun supports(fileName: String): Boolean {
        return fileName.contains("nubank", ignoreCase = true) &&
               fileName.endsWith(".csv", ignoreCase = true)
    }
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        // 1. Analyze file structure first
        println("📄 Parsing Nubank file: $fileName")
        println("📄 First lines: ${fileContent.lines().take(5)}")
        
        // 2. Implement parsing logic
        return fileContent.lines()
            .drop(1)  // Adjust based on header
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    parseNubankLine(line)
                } catch (e: Exception) {
                    println("⚠️ Failed to parse line: $line")
                    null
                }
            }
    }
    
    private fun parseNubankLine(line: String): Transaction {
        // Implement based on Nubank CSV format
        // Check: delimiter (comma, semicolon, tab)
        // Check: date format (DD/MM/YYYY, YYYY-MM-DD)
        // Check: amount format (Brazilian: 1.234,56 vs American: 1,234.56)
    }
}
```

### Step 2: Register in Repository

```kotlin
// shared/src/commonMain/kotlin/com/financehub/data/repository/TransactionRepositoryImpl.kt
class TransactionRepositoryImpl : TransactionRepository {
    
    private val parsers: List<FileParser> = listOf(
        ItauCreditCardParser(),
        ItauBankStatementParser(),
        PicPayStatementParser(),
        NubankCreditCardParser()  // Add here
    )
    
    override suspend fun parseFile(content: String, fileName: String): Result<List<Transaction>> {
        val parser = parsers.find { it.supports(fileName) }
            ?: return Result.failure(UnsupportedFileException(fileName))
        
        return runCatching {
            parser.parse(content, fileName)
        }
    }
}
```

### Step 3: Add Tests

```kotlin
// shared/src/commonTest/kotlin/com/financehub/data/parser/NubankParserTest.kt
class NubankCreditCardParserTest {
    private val parser = NubankCreditCardParser()
    
    @Test
    fun `supports Nubank CSV files`() {
        assertTrue(parser.supports("nubank-2025-01.csv"))
        assertTrue(parser.supports("Nubank_Statement.csv"))
        assertFalse(parser.supports("itau-fatura.csv"))
    }
    
    @Test
    fun `parses valid Nubank CSV`() = runTest {
        val content = """
            date,description,amount
            2025-01-15,UBER *TRIP,25.50
            2025-01-16,NETFLIX.COM,-15.90
        """.trimIndent()
        
        val transactions = parser.parse(content, "nubank.csv")
        
        assertEquals(2, transactions.size)
        assertEquals(-25.50, transactions[0].amount)
    }
}
```

## Parser Conventions

### Naming
- `{Bank}{Type}Parser.kt`
- Examples: `ItauCreditCardParser`, `NubankAccountParser`, `BradescoInvestmentParser`

### Value Handling

⚠️ **CRITICAL**: Always check the amount format!

| Format | Example | Decimal | Thousands |
|--------|---------|---------|-----------|
| American | `1,234.56` | `.` | `,` |
| Brazilian | `1.234,56` | `,` | `.` |

```kotlin
// American format (common in Itau exports)
fun parseAmericanAmount(value: String): Double {
    return value.replace(",", "").toDouble()
}

// Brazilian format
fun parseBrazilianAmount(value: String): Double {
    return value.replace(".", "").replace(",", ".").toDouble()
}
```

### Date Handling

```kotlin
import kotlinx.datetime.LocalDate

fun parseDate(dateStr: String): LocalDate {
    // DD/MM/YYYY (Brazilian)
    val parts = dateStr.split("/")
    return LocalDate(
        year = parts[2].toInt(),
        monthNumber = parts[1].toInt(),
        dayOfMonth = parts[0].toInt()
    )
}

// Or use ISO format: YYYY-MM-DD
fun parseIsoDate(dateStr: String): LocalDate {
    return LocalDate.parse(dateStr)
}
```

### Transaction Signs

| Type | Amount Sign | Example |
|------|-------------|---------|
| Expense | Negative | `-100.00` |
| Income/Refund | Positive | `+50.00` |

```kotlin
// Most credit card exports show expenses as positive
// Convert to our convention:
val amount = if (rawAmount > 0) -rawAmount else rawAmount.absoluteValue
```

## Category System

### Available Categories

```kotlin
enum class Category(val displayName: String, val keywords: List<String>) {
    ALIMENTACAO("Alimentação", listOf("restaurante", "ifood", "uber eats", "mercado", "padaria")),
    TRANSPORTE("Transporte", listOf("uber", "99", "posto", "combustivel", "estacionamento")),
    MORADIA("Moradia", listOf("aluguel", "condominio", "luz", "agua", "gas")),
    SAUDE("Saúde", listOf("farmacia", "hospital", "medico", "drogaria", "clinica")),
    LAZER("Lazer", listOf("netflix", "spotify", "cinema", "teatro", "bar")),
    EDUCACAO("Educação", listOf("curso", "livro", "escola", "faculdade", "udemy")),
    VESTUARIO("Vestuário", listOf("roupa", "calcado", "renner", "zara", "nike")),
    ELETRONICOS("Eletrônicos", listOf("apple", "samsung", "kabum", "amazon")),
    SERVICOS("Serviços", listOf("assinatura", "mensalidade", "taxa")),
    OUTROS("Outros", emptyList())
}
```

### Auto-Categorization

```kotlin
fun categorize(description: String): Category {
    val lower = description.lowercase()
    return Category.entries.find { category ->
        category.keywords.any { keyword -> lower.contains(keyword) }
    } ?: Category.OUTROS
}
```

---

## Testing Guidelines

### Parser Tests

```kotlin
@Test
fun `handles empty file gracefully`() = runTest {
    val result = parser.parse("", "empty.csv")
    assertTrue(result.isEmpty())
}

@Test
fun `handles malformed lines`() = runTest {
    val content = """
        date,description,amount
        invalid line
        15/01/2025,Valid,100.00
    """.trimIndent()
    
    val result = parser.parse(content, "test.csv")
    assertEquals(1, result.size)  // Only valid line parsed
}

@Test
fun `correctly categorizes transactions`() = runTest {
    val content = "15/01/2025,IFOOD *RESTAURANTE,50.00"
    val result = parser.parse(content, "test.csv")
    assertEquals(Category.ALIMENTACAO, result[0].category)
}
```

---

**When adding new parsers, always test with real sample files first to understand the exact format.**
