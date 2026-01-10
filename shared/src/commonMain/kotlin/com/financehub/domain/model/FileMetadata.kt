package com.financehub.domain.model

import kotlinx.serialization.Serializable

/**
 * Metadata de arquivo selecionado para import
 */
@Serializable
data class FileMetadata(
    val filePath: String,
    val customLabel: String, // Nome customizado pelo usuário (obrigatório)
    val detectedSource: DataSource // Fonte detectada automaticamente pelo parser
) {
    val fileName: String get() = filePath.split("/").last()
}

/**
 * Mapeamento de padrões de arquivo para labels sugeridas
 */
@Serializable
data class SourceMapping(
    val patterns: List<PatternMapping> = emptyList()
) {
    companion object {
        fun default() = SourceMapping(emptyList())
    }
}

@Serializable
data class PatternMapping(
    val filePattern: String, // Ex: "*itau*card*.csv"
    val suggestedLabel: String, // Ex: "Cartão Pessoal Itaú"
    val usageCount: Int = 1 // Quantas vezes foi usado
)
