package com.financehub.presentation

import com.financehub.domain.model.ChangeLogEntry
import com.financehub.domain.model.ConsolidatedReport
import com.financehub.domain.model.FileMetadata
import com.financehub.domain.model.IgnorePatterns
import com.financehub.domain.model.MonthReport
import com.financehub.domain.model.Transaction

/**
 * Estado da UI principal
 */
data class FinanceState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val transactions: List<Transaction> = emptyList(),
    val monthReports: List<MonthReport> = emptyList(),
    val consolidatedReport: ConsolidatedReport? = null,
    val selectedMonth: String? = null,
    val directoryPath: String = "",
    
    // Preview mode properties
    val previewMode: Boolean = false,
    val selectedFiles: List<String> = emptyList(),
    val fileMetadata: Map<String, FileMetadata> = emptyMap(), // File path -> metadata
    val showFileNamingDialog: Boolean = false,
    val pendingTransactions: List<Transaction> = emptyList(),
    val editedTransactions: Map<String, Transaction> = emptyMap(),
    val changeLog: List<ChangeLogEntry> = emptyList(),
    val searchQuery: String = "",
    
    // Ignore patterns
    val ignorePatterns: IgnorePatterns = IgnorePatterns.default(),
    val showIgnorePatternsDialog: Boolean = false,
    
    // Historical data
    val savedMonths: List<String> = emptyList()
)

/**
 * Eventos da UI
 */
sealed class FinanceEvent {
    data class LoadDirectory(val path: String) : FinanceEvent()
    data class SelectMonth(val yearMonth: String) : FinanceEvent()
    object GenerateReports : FinanceEvent()
    object ClearError : FinanceEvent()
    
    // Preview mode events
    data class SelectFiles(val filePaths: List<String>) : FinanceEvent()
    data class ConfirmFileMetadata(val metadata: Map<String, FileMetadata>) : FinanceEvent()
    object CancelFileNaming : FinanceEvent()
    object PreviewSelectedFiles : FinanceEvent()
    data class EditTransactionCategory(val transactionId: String, val newCategory: com.financehub.domain.model.Category) : FinanceEvent()
    data class ToggleTransactionType(val transactionId: String) : FinanceEvent()
    data class ToggleTransactionIgnored(val transactionId: String) : FinanceEvent()
    object ConfirmPreview : FinanceEvent()
    object CancelPreview : FinanceEvent()
    data class UpdateSearchQuery(val query: String) : FinanceEvent()
    object ExportChangeLog : FinanceEvent()
    
    // Ignore patterns events
    object ShowIgnorePatternsDialog : FinanceEvent()
    object HideIgnorePatternsDialog : FinanceEvent()
    data class AddIgnorePattern(val pattern: String) : FinanceEvent()
    data class RemoveIgnorePattern(val pattern: String) : FinanceEvent()
    data class AddAdvancedIgnoreRule(
        val pattern: String,
        val minAmount: Double,
        val requiresIntegerValue: Boolean,
        val description: String
    ) : FinanceEvent()
    data class RemoveAdvancedIgnoreRule(val rule: com.financehub.domain.model.AdvancedIgnoreRule) : FinanceEvent()    
    // Historical data events
    object LoadHistoricalData : FinanceEvent()
    object ClearReports : FinanceEvent()
    object ClearAllData : FinanceEvent()
}
