package com.financehub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                
                // Modo normal - sem preview
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Abas para escolher modo
                        var selectedTab by remember { mutableStateOf(0) }
                        
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Preview de Arquivos") },
                                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Carregar Diretório (Legado)") },
                                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        when (selectedTab) {
                            0 -> {
                                // Nova interface de preview
                                FileSelectionScreen(
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
                            1 -> {
                                // Interface legada de carregar diretório
                                LegacyDirectoryMode(
                                    state = state,
                                    viewModel = viewModel,
                                    defaultPath = defaultPath
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegacyDirectoryMode(
    state: com.financehub.presentation.FinanceState,
    viewModel: FinanceViewModel,
    defaultPath: String
) {
    // Auto-carregar se tem path padrão (apenas uma vez)
    LaunchedEffect(defaultPath) {
        if (defaultPath.isNotEmpty() && state.directoryPath.isEmpty() && state.transactions.isEmpty()) {
            viewModel.onEvent(FinanceEvent.LoadDirectory(defaultPath))
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Diretório input
        DirectoryInput(
            path = state.directoryPath,
            onLoadClick = { path ->
                viewModel.onEvent(FinanceEvent.LoadDirectory(path))
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loading ou conteúdo
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            state.error != null -> {
                ErrorCard(
                    message = state.error ?: "",
                    onDismiss = { viewModel.onEvent(FinanceEvent.ClearError) }
                )
            }
            
            state.consolidatedReport != null -> {
                ReportContent(viewModel, state)
            }
            
            else -> {
                Text(
                    "Selecione um diretório com seus extratos financeiros",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DirectoryInput(
    path: String,
    onLoadClick: (String) -> Unit
) {
    var inputPath by remember(path) { mutableStateOf(path) }
    
    // Atualiza inputPath quando path muda
    LaunchedEffect(path) {
        if (path.isNotEmpty() && inputPath != path) {
            inputPath = path
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputPath,
            onValueChange = { inputPath = it },
            label = { Text("Diretório") },
            placeholder = { Text("/Users/joaogabriel/Notas") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        
        Button(
            onClick = { onLoadClick(inputPath) },
            enabled = true
        ) {
            Text("Carregar")
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
