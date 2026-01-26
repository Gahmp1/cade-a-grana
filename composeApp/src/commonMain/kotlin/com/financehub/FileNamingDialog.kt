package com.financehub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*
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
    // Detecta automaticamente a fonte de cada arquivo
    val detectedSources = remember {
        selectedFiles.associateWith { filePath ->
            detectSourceFromFilename(filePath)
        }
    }
    
    // Armazena os nomes customizados de cada arquivo (já inicializado com sugestões)
    val customLabels = remember {
        mutableStateMapOf<String, String>().apply {
            selectedFiles.forEach { filePath ->
                val source = detectedSources[filePath] ?: DataSource.MANUAL
                this[filePath] = getSuggestedLabel(source)
            }
        }
    }
    
    // Armazena o owner selecionado para cada arquivo
    val selectedOwners = remember { mutableStateMapOf<String, String?>() }
    
    // Household para opções de owner
    val household = remember { com.financehub.domain.model.Household() }
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column {
                DSText(
                    text = "Identificar Arquivos",
                    variant = TextVariant.HEADLINE
                )
                DSText(
                    text = "Dê um nome para cada arquivo importado",
                    variant = TextVariant.CAPTION
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
                        currentOwner = selectedOwners[filePath],
                        household = household,
                        onLabelChange = { newLabel ->
                            customLabels[filePath] = newLabel
                        },
                        onOwnerChange = { newOwner ->
                            selectedOwners[filePath] = newOwner
                        }
                    )
                }
                
                item {
                    DSCard(variant = CardVariant.INFO) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = DSColors.info,
                                    modifier = Modifier.size(20.dp)
                                )
                                DSText(
                                    text = "💡 Dica",
                                    variant = TextVariant.TITLE
                                )
                            }
                            
                            DSText(
                                text = "Você pode editar esses nomes. Exemplos:",
                                variant = TextVariant.BODY
                            )
                            DSText(text = "• \"Cartão Visa João\" ou \"Cartão Itaú Daiane\"", variant = TextVariant.CAPTION)
                            DSText(text = "• \"Conta Corrente Conjunta\"", variant = TextVariant.CAPTION)
                            
                            HorizontalDivider(color = DSColors.Dark.borderSubtle)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = DSColors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                DSText(
                                    text = "Escolha abaixo de quem são as contas de cada arquivo (João, Daiane ou Compartilhado)",
                                    variant = TextVariant.CAPTION
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            DSButton(
                onClick = {
                    val metadata = selectedFiles.associateWith { filePath ->
                        FileMetadata(
                            filePath = filePath,
                            customLabel = customLabels[filePath]?.trim() ?: "",
                            detectedSource = detectedSources[filePath] ?: DataSource.MANUAL,
                            owner = selectedOwners[filePath]
                        )
                    }
                    onConfirm(metadata)
                },
                enabled = selectedFiles.all { filePath ->
                    customLabels[filePath]?.trim()?.isNotBlank() == true
                },
                variant = ButtonVariant.PRIMARY
            ) {
                Text("Confirmar e Fazer Preview")
            }
        },
        dismissButton = {
            DSTextButton(onClick = onCancel) {
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
    currentOwner: String?,
    household: com.financehub.domain.model.Household,
    onLabelChange: (String) -> Unit,
    onOwnerChange: (String?) -> Unit
) {
    val file = File(filePath)
    val fileName = file.name
    var showOwnerMenu by remember { mutableStateOf(false) }
    
    DSCard(variant = CardVariant.DEFAULT) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DSSpacing.sm)
        ) {
            // Número e nome do arquivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = "Arquivo $index",
                    variant = TextVariant.TITLE
                )
                
                // Badge com fonte detectada
                DSBadge(
                    text = detectedSource.displayName,
                    variant = BadgeVariant.INFO
                )
            }
            
            // Nome do arquivo (truncado)
            DSText(
                text = fileName,
                variant = TextVariant.CAPTION
            )
            
            // Campo de texto obrigatório
            DSTextField(
                value = currentLabel,
                onValueChange = onLabelChange,
                label = "Nome para identificar *",
                placeholder = "Ex: Cartão Pessoal Itaú",
                modifier = Modifier.fillMaxWidth(),
                isError = currentLabel.trim().isBlank()
            )
            
            // Seletor de dono (Owner)
            DSText(
                text = "De quem é esta conta?",
                variant = TextVariant.LABEL
            )
            
            DSButton(
                onClick = { showOwnerMenu = true },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.SECONDARY,
                leadingIcon = Icons.Default.Person,
                trailingIcon = Icons.Default.ArrowDropDown
            ) {
                Text(currentOwner ?: "Não atribuído")
            }
            
            // Dropdown menu para selecionar owner
            DropdownMenu(
                expanded = showOwnerMenu,
                onDismissRequest = { showOwnerMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Não atribuir agora") },
                    onClick = {
                        onOwnerChange(null)
                        showOwnerMenu = false
                    }
                )
                
                household.members.forEach { member ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DSSpacing.sm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            parseColorHex(member.colorHex),
                                            DSRadius.xs
                                        )
                                )
                                Text(member.name)
                            }
                        },
                        onClick = {
                            onOwnerChange(member.name)
                            showOwnerMenu = false
                        }
                    )
                }
                
                DropdownMenuItem(
                    text = { Text(household.sharedLabel) },
                    onClick = {
                        onOwnerChange(household.sharedLabel)
                        showOwnerMenu = false
                    }
                )
            }
        }
    }
}

/**
 * Parse hex color string to Compose Color
 */
private fun parseColorHex(hex: String): androidx.compose.ui.graphics.Color {
    val hexColor = hex.removePrefix("#")
    return androidx.compose.ui.graphics.Color(hexColor.toLong(16) or 0xFF000000)
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
        fileName.contains("nubank") && fileName.contains("fatura") -> DataSource.NUBANK_CREDIT_CARD
        fileName.contains("nubank") -> DataSource.NUBANK_BANK_STATEMENT
        fileName.contains("inter") -> DataSource.INTER_BANK_STATEMENT
        fileName.contains("c6") -> DataSource.C6_BANK_STATEMENT
        fileName.contains("bradesco") && fileName.contains("fatura") -> DataSource.BRADESCO_CREDIT_CARD
        fileName.contains("bradesco") -> DataSource.BRADESCO_BANK_STATEMENT
        fileName.contains("mercado") || fileName.contains("mp_") -> DataSource.MERCADOPAGO_STATEMENT
        fileName.contains("santander") && fileName.contains("fatura") -> DataSource.SANTANDER_CREDIT_CARD
        fileName.contains("santander") -> DataSource.SANTANDER_BANK_STATEMENT
        fileName.contains("caixa") || fileName.contains("cef") -> DataSource.CAIXA_BANK_STATEMENT
        fileName.contains("bb_") || fileName.contains("bancodobrasil") || 
            fileName.contains("banco_brasil") || fileName.contains("banco-brasil") -> DataSource.BANCO_BRASIL_STATEMENT
        else -> DataSource.MANUAL
    }
}

/**
 * Gera sugestão de label baseado no DataSource detectado
 */
private fun getSuggestedLabel(source: DataSource): String {
    return when (source) {
        DataSource.ITAU_CREDIT_CARD -> "Cartão Itaú"
        DataSource.ITAU_BANK_STATEMENT -> "Extrato Itaú"
        DataSource.PICPAY_STATEMENT -> "Extrato PicPay"
        DataSource.NUBANK_CREDIT_CARD -> "Cartão Nubank"
        DataSource.NUBANK_BANK_STATEMENT -> "Extrato Nubank"
        DataSource.INTER_BANK_STATEMENT -> "Extrato Inter"
        DataSource.C6_BANK_STATEMENT -> "Extrato C6"
        DataSource.BRADESCO_CREDIT_CARD -> "Cartão Bradesco"
        DataSource.BRADESCO_BANK_STATEMENT -> "Extrato Bradesco"
        DataSource.MERCADOPAGO_STATEMENT -> "Extrato Mercado Pago"
        DataSource.SANTANDER_CREDIT_CARD -> "Cartão Santander"
        DataSource.SANTANDER_BANK_STATEMENT -> "Extrato Santander"
        DataSource.CAIXA_BANK_STATEMENT -> "Extrato Caixa"
        DataSource.BANCO_BRASIL_STATEMENT -> "Extrato Banco do Brasil"
        DataSource.MANUAL, DataSource.UNKNOWN -> ""
    }
}
