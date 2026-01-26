package com.financehub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*
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
                // Modo de visualização detalhada de um mês
                state.selectedMonthDetail != null -> {
                    val monthTransactions = state.transactions.filter {
                        "${it.date.year}-${it.date.monthNumber.toString().padStart(2, '0')}" == state.selectedMonthDetail
                    }
                    MonthDetailView(
                        yearMonth = state.selectedMonthDetail!!,
                        transactions = monthTransactions,
                        household = state.household,
                        onEvent = viewModel::onEvent,
                        onClose = { viewModel.onEvent(FinanceEvent.CloseMonthDetail) }
                    )
                }
                
                // Modo Household Dashboard
                state.showHouseholdView -> {
                    HouseholdDashboard(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
                
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
            
            // Dialog de nomeação de arquivos
            if (state.showFileNamingDialog) {
                FileNamingDialog(
                    selectedFiles = state.selectedFiles,
                    onConfirm = { metadata ->
                        viewModel.onEvent(FinanceEvent.ConfirmFileMetadata(metadata))
                    },
                    onCancel = {
                        viewModel.onEvent(FinanceEvent.CancelFileNaming)
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    DSCard(
        variant = CardVariant.ERROR
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSText(
                text = message,
                variant = TextVariant.BODY,
                color = DSColors.Expense.light,
                modifier = Modifier.weight(1f)
            )
            
            DSButton(
                onClick = onDismiss,
                variant = ButtonVariant.GHOST
            ) {
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
            .padding(DSSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.lg)
    ) {
        // Botão Voltar e info de meses salvos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSButton(
                onClick = { viewModel.onEvent(FinanceEvent.ClearReports) },
                variant = ButtonVariant.SECONDARY,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack
            ) {
                Text("Voltar")
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)) {
                // Botão Saúde da Casa
                DSButton(
                    onClick = { viewModel.onEvent(FinanceEvent.ToggleHouseholdView) },
                    variant = ButtonVariant.POSITIVE,
                    leadingIcon = Icons.Default.Home
                ) {
                    Text("Saúde da Casa")
                }
                
                if (state.savedMonths.isNotEmpty()) {
                    DSText(
                        text = "💾 ${state.savedMonths.size} meses",
                        variant = TextVariant.CAPTION,
                        color = DSColors.Income.main,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
        
        // Resumo consolidado
        ConsolidatedSummaryCard(consolidated)
        
        Spacer(modifier = Modifier.height(DSSpacing.sm))
        
        // Lista de meses
        DSText(
            text = "Relatórios Mensais",
            variant = TextVariant.HEADLINE
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            items(state.monthReports) { monthReport ->
                MonthReportCard(
                    monthReport = monthReport,
                    isSelected = monthReport.yearMonth == state.selectedMonth,
                    onClick = {
                        viewModel.onEvent(FinanceEvent.SelectMonth(monthReport.yearMonth))
                    },
                    onViewDetails = {
                        viewModel.onEvent(FinanceEvent.ShowMonthDetail(monthReport.yearMonth))
                    }
                )
            }
        }
    }
}
