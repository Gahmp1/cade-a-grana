package com.financehub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financehub.domain.model.DataSource
import com.financehub.domain.model.FileMetadata
import com.financehub.presentation.FinanceEvent
import java.io.File

@Composable
fun FileNamingDialog(
    selectedFiles: List<String>,
    onConfirm: (Map<String, FileMetadata>) -> Unit,
    onCancel: () -> Unit
) {
    // Armazena os nomes customizados de cada arquivo
    val customLabels = remember { mutableStateMapOf<String, String>() }
    
    // Detecta automaticamente a fonte de cada arquivo
    val detectedSources = remember {
        selectedFiles.associateWith { filePath ->
            detectSourceFromFilename(filePath)
        }
    }
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column {
                Text(
                    "Identificar Arquivos",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Dê um nome para cada arquivo importado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(selectedFiles) { index, filePath ->
                    FileNamingItem(
                        index = index + 1,
                        filePath = filePath,
                        detectedSource = detectedSources[filePath] ?: DataSource.MANUAL,
                        currentLabel = customLabels[filePath] ?: "",
                        onLabelChange = { newLabel ->
                            customLabels[filePath] = newLabel
                        }
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Exemplos: \"Cartão Pessoal Itaú\", \"Conta Corrente\", \"PicPay João\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val metadata = selectedFiles.associateWith { filePath ->
                        FileMetadata(
                            filePath = filePath,
                            customLabel = customLabels[filePath]?.trim() ?: "",
                            detectedSource = detectedSources[filePath] ?: DataSource.MANUAL
                        )
                    }
                    onConfirm(metadata)
                },
                enabled = selectedFiles.all { filePath ->
                    customLabels[filePath]?.trim()?.isNotBlank() == true
                }
            ) {
                Text("Confirmar e Fazer Preview")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileNamingItem(
    index: Int,
    filePath: String,
    detectedSource: DataSource,
    currentLabel: String,
    onLabelChange: (String) -> Unit
) {
    val file = File(filePath)
    val fileName = file.name
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Número e nome do arquivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Arquivo $index",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Badge com fonte detectada
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        detectedSource.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Nome do arquivo (truncado)
            Text(
                fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            
            // Campo de texto obrigatório
            OutlinedTextField(
                value = currentLabel,
                onValueChange = onLabelChange,
                label = { Text("Nome para identificar *") },
                placeholder = { Text("Ex: Cartão Pessoal Itaú") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = currentLabel.trim().isBlank(),
                supportingText = if (currentLabel.trim().isBlank()) {
                    { Text("Campo obrigatório", color = MaterialTheme.colorScheme.error) }
                } else null
            )
        }
    }
}

/**
 * Detecta automaticamente a fonte baseado no nome do arquivo
 */
private fun detectSourceFromFilename(filePath: String): DataSource {
    val fileName = File(filePath).name.lowercase()
    
    return when {
        fileName.contains("itau") && fileName.contains("fatura") -> DataSource.ITAU_CREDIT_CARD
        fileName.contains("itau") && fileName.contains("extrato") -> DataSource.ITAU_BANK_STATEMENT
        fileName.contains("picpay") -> DataSource.PICPAY_STATEMENT
        else -> DataSource.MANUAL
    }
}
