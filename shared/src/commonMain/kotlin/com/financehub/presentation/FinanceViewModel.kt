package com.financehub.presentation

import com.financehub.domain.model.Category
import com.financehub.domain.model.ChangeLog
import com.financehub.domain.model.ChangeLogEntry
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
    
    private val json = Json { prettyPrint = true }
    
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
            is FinanceEvent.ConfirmPreview -> confirmPreview()
            is FinanceEvent.CancelPreview -> cancelPreview()
            is FinanceEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is FinanceEvent.ExportChangeLog -> exportChangeLog()
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
            
            previewTransactionsUseCase(selectedFiles)
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
            
            // Usar transações editadas como finais
            val finalTransactions = current.pendingTransactions
            
            _state.value = current.copy(
                isLoading = false,
                previewMode = false,
                transactions = finalTransactions,
                pendingTransactions = emptyList(),
                editedTransactions = emptyMap(),
                selectedFiles = emptyList()
            )
            
            // Auto-gerar relatórios
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
}
