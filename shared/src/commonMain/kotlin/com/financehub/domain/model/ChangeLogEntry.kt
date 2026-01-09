package com.financehub.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Representa uma mudança feita em uma transação durante o preview
 */
@Serializable
data class ChangeLogEntry(
    val transactionId: String,
    val date: LocalDate,
    val description: String,
    val amount: Double,
    val source: String,
    val originalCategory: String,
    val newCategory: String,
    val originalType: String, // "Entrada" ou "Saída"
    val newType: String,
    val originalIgnored: Boolean,
    val newIgnored: Boolean,
    val timestamp: String
)

/**
 * Log consolidado de todas as mudanças
 */
@Serializable
data class ChangeLog(
    val changes: List<ChangeLogEntry>,
    val totalChanges: Int,
    val categoryChanges: Int,
    val typeChanges: Int
) {
    companion object {
        fun fromEntries(entries: List<ChangeLogEntry>): ChangeLog {
            val categoryChanges = entries.count { it.originalCategory != it.newCategory }
            val typeChanges = entries.count { it.originalType != it.newType }
            val ignoredChanges = entries.count { it.originalIgnored != it.newIgnored }
            return ChangeLog(
                changes = entries,
                totalChanges = entries.size,
                categoryChanges = categoryChanges,
                typeChanges = typeChanges + ignoredChanges
            )
        }
    }
}
