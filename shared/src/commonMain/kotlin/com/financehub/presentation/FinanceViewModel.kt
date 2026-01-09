package com.financehub.presentation

import com.financehub.domain.model.Category
import com.financehub.domain.model.ChangeLog
import com.financehub.domain.model.ChangeLogEntry
import com.financehub.domain.model.Transaction
import com.financehub.domain.usecase.GenerateConsolidatedReportUseCase
import com.financehub.domain.usecase.GenerateMonthReportsUseCase
import com.financehub.domain.usecase.LoadTransactionsUseCase
import com.financehub.domain.usecase.PreviewTransactionsUseCase
import com.financehub.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * ViewModel principal da aplicação
 */
class FinanceViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase,
    private val generateMonthReportsUseCase: GenerateMonthReportsUseCase,
    private val generateConsolidatedReportUseCase: GenerateConsolidatedReportUseCase,
    private val previewTransactionsUseCase: PreviewTransactionsUseCase,
    private val repository: TransactionRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _state = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()
    
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    
    init {
        loadIgnorePatterns()
        loadSavedMonthsList()
    }
    
    fun onEvent(event: FinanceEvent) {
        when (event) {
            is FinanceEvent.LoadDirectory -> loadDirectory(event.path)
            is FinanceEvent.SelectMonth -> selectMonth(event.yearMonth)
            is FinanceEvent.GenerateReports -> generateReports()
            is FinanceEvent.ClearError -> clearError()
            is FinanceEvent.SelectFiles -> selectFiles(event.filePaths)
            is FinanceEvent.PreviewSelectedFiles -> previewSelectedFiles()
            is FinanceEvent.EditTransactionCategory -> editTransactionCategory(event.transactionId, event.newCategory)
            is FinanceEvent.ToggleTransactionType -> toggleTransactionType(event.transactionId)
            is FinanceEvent.ToggleTransactionIgnored -> toggleTransactionIgnored(event.transactionId)
            is FinanceEvent.ConfirmPreview -> confirmPreview()
            is FinanceEvent.CancelPreview -> cancelPreview()
            is FinanceEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is FinanceEvent.ExportChangeLog -> exportChangeLog()
            is FinanceEvent.ShowIgnorePatternsDialog -> showIgnorePatternsDialog()
            is FinanceEvent.HideIgnorePatternsDialog -> hideIgnorePatternsDialog()
            is FinanceEvent.AddIgnorePattern -> addIgnorePattern(event.pattern)
            is FinanceEvent.RemoveIgnorePattern -> removeIgnorePattern(event.pattern)
            is FinanceEvent.AddAdvancedIgnoreRule -> addAdvancedIgnoreRule(
                event.pattern, event.minAmount, event.requiresIntegerValue, event.description
            )
            is FinanceEvent.RemoveAdvancedIgnoreRule -> removeAdvancedIgnoreRule(event.rule)
            is FinanceEvent.LoadHistoricalData -> loadHistoricalData()
            is FinanceEvent.ClearReports -> clearReports()
        }
    }
    
    private fun loadDirectory(path: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                directoryPath = path
            )
            
            loadTransactionsUseCase(path)
                .onSuccess { transactions ->
                    println("✅ Carregadas ${transactions.size} transações")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        transactions = transactions,
                        error = if (transactions.isEmpty()) "Nenhuma transação encontrada. Verifique o formato dos arquivos." else null
                    )
                    
                    // Auto-gerar relatórios após carregar
                    generateReports()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao carregar transações"
                    )
                }
        }
    }
    
    private fun generateReports() {
        coroutineScope.launch {
            val transactions = _state.value.transactions
            if (transactions.isEmpty()) {
                _state.value = _state.value.copy(
                    error = "Carregue um diretório primeiro"
                )
                return@launch
            }
            
            _state.value = _state.value.copy(isLoading = true)
            
            // Gerar relatórios mensais
            generateMonthReportsUseCase(transactions)
                .onSuccess { monthReports ->
                    // Gerar relatório consolidado
                    generateConsolidatedReportUseCase(transactions)
                        .onSuccess { consolidated ->
                            _state.value = _state.value.copy(
                                isLoading = false,
                                monthReports = monthReports,
                                consolidatedReport = consolidated,
                                selectedMonth = monthReports.firstOrNull()?.yearMonth
                            )
                        }
                        .onFailure { error ->
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = error.message ?: "Erro ao gerar relatório consolidado"
                            )
                        }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao gerar relatórios mensais"
                    )
                }
        }
    }
    
    private fun selectMonth(yearMonth: String) {
        _state.value = _state.value.copy(selectedMonth = yearMonth)
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    // ========== Preview Mode Methods ==========
    
    private fun selectFiles(filePaths: List<String>) {
        _state.value = _state.value.copy(
            selectedFiles = filePaths,
            error = null
        )
    }
    
    private fun previewSelectedFiles() {
        coroutineScope.launch {
            val selectedFiles = _state.value.selectedFiles
            println("\n🚀 ViewModel: Iniciando previewSelectedFiles()")
            println("   Arquivos selecionados: ${selectedFiles.size}")
            
            if (selectedFiles.isEmpty()) {
                println("❌ ViewModel: Nenhum arquivo selecionado")
                _state.value = _state.value.copy(
                    error = "Selecione pelo menos um arquivo"
                )
                return@launch
            }
            
            selectedFiles.forEachIndexed { i, file ->
                println("   ${i + 1}. ${file.split("/").last()}")
            }
            
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            
            println("⏳ Chamando PreviewTransactionsUseCase...")
            
            previewTransactionsUseCase(selectedFiles, _state.value.ignorePatterns)
                .onSuccess { transactions ->
                    println("✅ ViewModel: Preview concluído com ${transactions.size} transações")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        previewMode = true,
                        pendingTransactions = transactions,
                        editedTransactions = emptyMap(),
                        changeLog = emptyList()
                    )
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Erro ao fazer preview dos arquivos"
                    println("❌ ViewModel: Falha no preview - $errorMsg")
                    error.printStackTrace()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
        }
    }
    
    private fun editTransactionCategory(transactionId: String, newCategory: Category) {
        val current = _state.value
        val originalTransaction = current.pendingTransactions.find { it.id == transactionId }
            ?: return
        
        val editedTransaction = originalTransaction.copy(category = newCategory)
        val updatedEdited = current.editedTransactions + (transactionId to editedTransaction)
        
        // Atualizar lista de pendentes
        val updatedPending = current.pendingTransactions.map { 
            if (it.id == transactionId) editedTransaction else it
        }
        
        // Criar entrada de log se categoria mudou
        val newChangeLog = if (originalTransaction.category != newCategory) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val entry = ChangeLogEntry(
                transactionId = transactionId,
                date = originalTransaction.date,
                description = originalTransaction.description,
                amount = originalTransaction.amount,
                source = originalTransaction.source.displayName,
                originalCategory = originalTransaction.category.displayName,
                newCategory = newCategory.displayName,
                originalType = if (originalTransaction.isIncome) "Entrada" else "Saída",
                newType = if (originalTransaction.isIncome) "Entrada" else "Saída",
                originalIgnored = originalTransaction.isIgnored,
                newIgnored = originalTransaction.isIgnored,
                timestamp = now.toString()
            )
            
            // Remover entradas antigas do mesmo ID e adicionar nova
            current.changeLog.filter { it.transactionId != transactionId } + entry
        } else {
            current.changeLog
        }
        
        _state.value = current.copy(
            pendingTransactions = updatedPending,
            editedTransactions = updatedEdited,
            changeLog = newChangeLog
        )
    }
    
    private fun toggleTransactionType(transactionId: String) {
        val current = _state.value
        val originalTransaction = current.pendingTransactions.find { it.id == transactionId }
            ?: return
        
        // Inverter o sinal do valor
        val editedTransaction = originalTransaction.copy(amount = -originalTransaction.amount)
        val updatedEdited = current.editedTransactions + (transactionId to editedTransaction)
        
        // Atualizar lista de pendentes
        val updatedPending = current.pendingTransactions.map { 
            if (it.id == transactionId) editedTransaction else it
        }
        
        // Criar entrada de log
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val entry = ChangeLogEntry(
            transactionId = transactionId,
            date = originalTransaction.date,
            description = originalTransaction.description,
            amount = originalTransaction.amount,
            source = originalTransaction.source.displayName,
            originalCategory = originalTransaction.category.displayName,
            newCategory = editedTransaction.category.displayName,
            originalType = if (originalTransaction.isIncome) "Entrada" else "Saída",
            newType = if (editedTransaction.isIncome) "Entrada" else "Saída",
            originalIgnored = originalTransaction.isIgnored,
            newIgnored = editedTransaction.isIgnored,
            timestamp = now.toString()
        )
        
        // Remover entradas antigas do mesmo ID e adicionar nova
        val newChangeLog = current.changeLog.filter { it.transactionId != transactionId } + entry
        
        _state.value = current.copy(
            pendingTransactions = updatedPending,
            editedTransactions = updatedEdited,
            changeLog = newChangeLog
        )
    }
    
    private fun toggleTransactionIgnored(transactionId: String) {
        val current = _state.value
        val originalTransaction = current.pendingTransactions.find { it.id == transactionId }
            ?: return
        
        // Inverter o status de ignorado
        val editedTransaction = originalTransaction.copy(isIgnored = !originalTransaction.isIgnored)
        val updatedEdited = current.editedTransactions + (transactionId to editedTransaction)
        
        // Atualizar lista de pendentes
        val updatedPending = current.pendingTransactions.map { 
            if (it.id == transactionId) editedTransaction else it
        }
        
        // Criar entrada de log
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val entry = ChangeLogEntry(
            transactionId = transactionId,
            date = originalTransaction.date,
            description = originalTransaction.description,
            amount = originalTransaction.amount,
            source = originalTransaction.source.displayName,
            originalCategory = originalTransaction.category.displayName,
            newCategory = editedTransaction.category.displayName,
            originalType = if (originalTransaction.isIncome) "Entrada" else "Saída",
            newType = if (editedTransaction.isIncome) "Entrada" else "Saída",
            originalIgnored = originalTransaction.isIgnored,
            newIgnored = editedTransaction.isIgnored,
            timestamp = now.toString()
        )
        
        // Remover entradas antigas do mesmo ID e adicionar nova
        val newChangeLog = current.changeLog.filter { it.transactionId != transactionId } + entry
        
        _state.value = current.copy(
            pendingTransactions = updatedPending,
            editedTransactions = updatedEdited,
            changeLog = newChangeLog
        )
    }
    
    private fun confirmPreview() {
        coroutineScope.launch {
            val current = _state.value
            
            _state.value = current.copy(
                isLoading = true,
                error = null
            )
            
            // Usar transações editadas como finais, excluindo as ignoradas
            val finalTransactions = current.pendingTransactions.filter { !it.isIgnored }
            
            // Agrupar por mês e salvar
            val byMonth = finalTransactions.groupBy { 
                "${it.date.year}-${it.date.monthNumber.toString().padStart(2, '0')}"
            }
            
            println("💾 Salvando ${byMonth.size} mês(es)...")
            byMonth.forEach { (yearMonth, monthTransactions) ->
                // Carregar transações existentes do mês
                repository.loadTransactionsForMonth(yearMonth)
                    .onSuccess { existingTransactions ->
                        // Merge: remover duplicatas por ID
                        val existingIds = existingTransactions.map { it.id }.toSet()
                        val newTransactions = monthTransactions.filter { it.id !in existingIds }
                        val mergedTransactions = (existingTransactions + newTransactions)
                            .sortedBy { it.date }
                        
                        // Salvar transações mescladas
                        repository.saveTransactionsForMonth(yearMonth, mergedTransactions)
                    }
                    .onFailure {
                        // Se falhar ao carregar, apenas salva as novas
                        repository.saveTransactionsForMonth(yearMonth, monthTransactions)
                    }
            }
            
            _state.value = current.copy(
                isLoading = false,
                previewMode = false,
                transactions = finalTransactions,
                pendingTransactions = emptyList(),
                editedTransactions = emptyMap(),
                selectedFiles = emptyList()
            )
            
            // Recarregar meses salvos e auto-gerar relatórios
            loadHistoricalData()
            generateReports()
        }
    }
    
    private fun cancelPreview() {
        _state.value = _state.value.copy(
            previewMode = false,
            selectedFiles = emptyList(),
            pendingTransactions = emptyList(),
            editedTransactions = emptyMap(),
            changeLog = emptyList(),
            searchQuery = ""
        )
    }
    
    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
    
    private fun exportChangeLog() {
        coroutineScope.launch {
            val current = _state.value
            if (current.changeLog.isEmpty()) {
                _state.value = current.copy(
                    error = "Nenhuma mudança para exportar"
                )
                return@launch
            }
            
            try {
                val changeLog = ChangeLog.fromEntries(current.changeLog)
                val jsonContent = json.encodeToString(changeLog)
                
                val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    .toString().replace(":", "-").take(19)
                val filePath = "${current.directoryPath}/change-log-$timestamp.json"
                
                repository.saveChangeLog(jsonContent, filePath)
                    .onSuccess {
                        println("✅ Log exportado: $filePath")
                    }
                    .onFailure { error ->
                        _state.value = current.copy(
                            error = "Erro ao exportar log: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _state.value = current.copy(
                    error = "Erro ao serializar log: ${e.message}"
                )
            }
        }
    }
    
    private fun showIgnorePatternsDialog() {
        _state.value = _state.value.copy(showIgnorePatternsDialog = true)
    }
    
    private fun hideIgnorePatternsDialog() {
        _state.value = _state.value.copy(showIgnorePatternsDialog = false)
    }
    
    private fun addIgnorePattern(pattern: String) {
        if (pattern.isBlank()) return
        
        val current = _state.value.ignorePatterns
        val trimmedPattern = pattern.trim().lowercase()
        
        if (!current.patterns.contains(trimmedPattern)) {
            val updated = current.copy(
                patterns = current.patterns + trimmedPattern
            )
            _state.value = _state.value.copy(ignorePatterns = updated)
            
            // Salvar padrões
            saveIgnorePatterns(updated)
            
            // Reaplicar padrões nas transações do preview
            reapplyIgnorePatterns()
        }
    }
    
    private fun removeIgnorePattern(pattern: String) {
        val current = _state.value.ignorePatterns
        val updated = current.copy(
            patterns = current.patterns.filter { it != pattern }
        )
        _state.value = _state.value.copy(ignorePatterns = updated)
        
        // Salvar padrões
        saveIgnorePatterns(updated)
        
        // Reaplicar padrões nas transações do preview
        reapplyIgnorePatterns()
    }
    
    private fun addAdvancedIgnoreRule(
        pattern: String,
        minAmount: Double,
        requiresIntegerValue: Boolean,
        description: String
    ) {
        if (pattern.isBlank()) return
        
        val current = _state.value.ignorePatterns
        val newRule = com.financehub.domain.model.AdvancedIgnoreRule(
            pattern = pattern.trim(),
            minAmount = minAmount,
            requiresIntegerValue = requiresIntegerValue,
            description = description.trim()
        )
        
        val updated = current.copy(
            advancedRules = current.advancedRules + newRule
        )
        _state.value = _state.value.copy(ignorePatterns = updated)
        
        // Salvar padrões
        saveIgnorePatterns(updated)
        
        // Reaplicar padrões nas transações do preview
        reapplyIgnorePatterns()
    }
    
    private fun removeAdvancedIgnoreRule(rule: com.financehub.domain.model.AdvancedIgnoreRule) {
        val current = _state.value.ignorePatterns
        val updated = current.copy(
            advancedRules = current.advancedRules.filter { it != rule }
        )
        _state.value = _state.value.copy(ignorePatterns = updated)
        
        // Salvar padrões
        saveIgnorePatterns(updated)
        
        // Reaplicar padrões nas transações do preview
        reapplyIgnorePatterns()
    }
    
    private fun reapplyIgnorePatterns() {
        val current = _state.value
        
        // Se não estiver em preview mode, não fazer nada
        if (!current.previewMode || current.pendingTransactions.isEmpty()) {
            return
        }
        
        val patterns = current.ignorePatterns
        
        // Reaplicar padrões em todas as transações pendentes
        val updatedTransactions = current.pendingTransactions.map { transaction ->
            // Começar com transação limpa (sem flags de ignorado)
            val cleanTransaction = transaction.copy(isIgnored = false, needsReview = false)
            
            // Verificar padrão simples
            val matchesSimple = patterns.matchesSimplePattern(cleanTransaction.description)
            
            // Verificar regra avançada
            val matchedAdvancedRule = patterns.matchesAdvancedRule(
                cleanTransaction.description,
                cleanTransaction.amount
            )
            
            when {
                matchedAdvancedRule != null -> {
                    // Regra avançada: ignorar + marcar para revisão
                    cleanTransaction.copy(isIgnored = true, needsReview = true)
                }
                matchesSimple -> {
                    // Padrão simples: apenas ignorar
                    cleanTransaction.copy(isIgnored = true)
                }
                else -> cleanTransaction
            }
        }
        
        // Atualizar o estado com as transações atualizadas
        _state.value = current.copy(pendingTransactions = updatedTransactions)
        
        val ignoredCount = updatedTransactions.count { it.isIgnored }
        val reviewCount = updatedTransactions.count { it.needsReview }
        println("🔄 Padrões reaplicados: $ignoredCount ignoradas, $reviewCount para revisão")
    }
    
    private fun loadIgnorePatterns() {
        coroutineScope.launch {
            try {
                val homeDir = System.getProperty("user.home")
                val filePath = "$homeDir/.financehub/ignore-patterns.json"
                
                repository.loadTransactionsFromFile(filePath)
                // Se o arquivo existir, tentar ler como texto
                val file = java.io.File(filePath)
                if (file.exists()) {
                    val jsonContent = file.readText()
                    val patterns = json.decodeFromString<com.financehub.domain.model.IgnorePatterns>(jsonContent)
                    _state.value = _state.value.copy(ignorePatterns = patterns)
                    println("✅ Padrões carregados: ${patterns.patterns.size} simples, ${patterns.advancedRules.size} avançados")
                } else {
                    println("ℹ️ Nenhum padrão salvo encontrado, usando padrões default")
                }
            } catch (e: Exception) {
                println("⚠️ Erro ao carregar padrões: ${e.message}")
                // Manter padrões default em caso de erro
            }
        }
    }
    
    private fun saveIgnorePatterns(patterns: com.financehub.domain.model.IgnorePatterns) {
        coroutineScope.launch {
            try {
                val homeDir = System.getProperty("user.home")
                val configDir = "$homeDir/.financehub"
                val filePath = "$configDir/ignore-patterns.json"
                
                // Criar diretório se não existir
                java.io.File(configDir).mkdirs()
                
                val jsonContent = json.encodeToString(patterns)
                java.io.File(filePath).writeText(jsonContent)
                
                println("✅ Padrões de ignorar salvos: $filePath")
            } catch (e: Exception) {
                println("⚠️ Erro ao salvar padrões: ${e.message}")
            }
        }
    }
    
    private fun loadSavedMonthsList() {
        coroutineScope.launch {
            repository.loadAllSavedMonths()
                .onSuccess { months ->
                    _state.value = _state.value.copy(savedMonths = months)
                    if (months.isNotEmpty()) {
                        println("📅 Meses disponíveis: ${months.joinToString()}")
                    }
                }
                .onFailure { error ->
                    println("❌ Erro ao carregar lista de meses: ${error.message}")
                }
        }
    }
    
    private fun loadHistoricalData() {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            repository.loadAllSavedMonths()
                .onSuccess { months ->
                    _state.value = _state.value.copy(savedMonths = months)
                    
                    if (months.isNotEmpty()) {
                        println("📅 Meses históricos carregados: ${months.joinToString()}")
                        
                        // Carregar todas as transações históricas
                        val allHistoricalTransactions = mutableListOf<Transaction>()
                        months.forEach { yearMonth ->
                            repository.loadTransactionsForMonth(yearMonth)
                                .onSuccess { transactions ->
                                    allHistoricalTransactions.addAll(transactions)
                                }
                        }
                        
                        // Sempre atualizar as transações e gerar relatórios se há dados históricos
                        if (allHistoricalTransactions.isNotEmpty()) {
                            _state.value = _state.value.copy(
                                transactions = allHistoricalTransactions,
                                isLoading = false
                            )
                            generateReports()
                        } else {
                            _state.value = _state.value.copy(isLoading = false)
                        }
                    } else {
                        _state.value = _state.value.copy(isLoading = false)
                    }
                }
                .onFailure { error ->
                    println("❌ Erro ao carregar dados históricos: ${error.message}")
                    _state.value = _state.value.copy(isLoading = false)
                }
        }
    }
    
    private fun clearReports() {
        _state.value = _state.value.copy(
            transactions = emptyList(),
            monthReports = emptyList(),
            consolidatedReport = null,
            selectedMonth = null
        )
    }
}
