package com.financehub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.financehub.data.parser.FileReader
import com.financehub.data.repository.TransactionRepositoryImpl
import com.financehub.domain.usecase.GenerateConsolidatedReportUseCase
import com.financehub.domain.usecase.GenerateMonthReportsUseCase
import com.financehub.domain.usecase.LoadTransactionsUseCase
import com.financehub.domain.usecase.PreviewTransactionsUseCase
import com.financehub.presentation.FinanceEvent
import com.financehub.presentation.FinanceViewModel
import com.financehub.theme.FinanceHubTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun App(
    directoryPath: String = "",
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    onSelectFiles: () -> List<String> = { emptyList() }
) {
    // Setup DI
    val fileReader = remember { FileReader() }
    val repository = remember { TransactionRepositoryImpl(fileReader) }
    val loadTransactionsUseCase = remember { LoadTransactionsUseCase(repository) }
    val generateMonthReportsUseCase = remember { GenerateMonthReportsUseCase(repository) }
    val generateConsolidatedReportUseCase = remember { GenerateConsolidatedReportUseCase(repository) }
    val previewTransactionsUseCase = remember { PreviewTransactionsUseCase(repository) }
    
    val viewModel = remember {
        FinanceViewModel(
            loadTransactionsUseCase,
            generateMonthReportsUseCase,
            generateConsolidatedReportUseCase,
            previewTransactionsUseCase,
            repository,
            coroutineScope
        )
    }
    
    FinanceHubTheme {
        MainScreen(
            viewModel = viewModel,
            defaultPath = directoryPath,
            onSelectFiles = onSelectFiles
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel,
    defaultPath: String,
    onSelectFiles: () -> List<String>
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (state.previewMode) "Preview de Transações"
                        else "FinanceHub - Análise Financeira"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Modo preview ativo
                state.previewMode -> {
                    TransactionPreviewScreen(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
                
                // Modo relatórios - mostra quando há dados processados
                state.consolidatedReport != null -> {
                    ReportContent(
                        viewModel = viewModel,
                        state = state
                    )
                }
                
                // Modo Home - estado inicial
                else -> {
                    HomeScreen(
                        state = state,
                        onEvent = viewModel::onEvent,
                        onSelectFiles = {
                            val selectedFiles = onSelectFiles()
                            if (selectedFiles.isNotEmpty()) {
                                viewModel.onEvent(FinanceEvent.SelectFiles(selectedFiles))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    }
}

@Composable
fun ReportContent(
    viewModel: FinanceViewModel,
    state: com.financehub.presentation.FinanceState
) {
    val consolidated = state.consolidatedReport ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botão Voltar e info de meses salvos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.onEvent(FinanceEvent.ClearReports) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Voltar")
            }
            
            if (state.savedMonths.isNotEmpty()) {
                Text(
                    "💾 ${state.savedMonths.size} meses salvos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Resumo consolidado
        ConsolidatedSummaryCard(consolidated)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Lista de meses
        Text(
            "Relatórios Mensais",
            style = MaterialTheme.typography.titleLarge
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.monthReports) { monthReport ->
                MonthReportCard(
                    monthReport = monthReport,
                    isSelected = monthReport.yearMonth == state.selectedMonth,
                    onClick = {
                        viewModel.onEvent(FinanceEvent.SelectMonth(monthReport.yearMonth))
                    }
                )
            }
        }
    }
}
