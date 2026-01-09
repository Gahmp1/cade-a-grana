package com.financehub.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Regra avançada para ignorar transações com condições de valor
 */
@Serializable
data class AdvancedIgnoreRule(
    val pattern: String,
    val minAmount: Double,
    val requiresIntegerValue: Boolean = false,
    val description: String = ""
) {
    fun matches(transactionDescription: String, transactionAmount: Double): Boolean {
        val lowerDesc = transactionDescription.lowercase()
        val absAmount = abs(transactionAmount)
        
        // Verifica padrão de texto
        if (!lowerDesc.contains(pattern.lowercase())) {
            return false
        }
        
        // Verifica valor mínimo
        if (absAmount < minAmount) {
            return false
        }
        
        // Verifica se é valor redondo (múltiplo de 1000)
        if (requiresIntegerValue) {
            // Considera redondo se for múltiplo de 1000 (ex: 1000, 2000, 3000, 5000)
            val isRoundNumber = (absAmount % 1000 == 0.0)
            if (!isRoundNumber) {
                return false
            }
        }
        
        return true
    }
}

/**
 * Padrões para ignorar transações automaticamente
 */
@Serializable
data class IgnorePatterns(
    val patterns: List<String> = listOf(
        "aporte",
        "credito via p2p",
        "transferência recebida da carteira",
        "resgate",
        "aplicação",
        "rendimento"
    ),
    val advancedRules: List<AdvancedIgnoreRule> = emptyList()
) {
    /**
     * Verifica se uma descrição contém algum padrão simples de ignorar
     */
    fun matchesSimplePattern(description: String): Boolean {
        val lowerDesc = description.lowercase()
        return patterns.any { pattern ->
            lowerDesc.contains(pattern.lowercase())
        }
    }
    
    /**
     * Verifica se uma transação corresponde a alguma regra avançada
     */
    fun matchesAdvancedRule(description: String, amount: Double): AdvancedIgnoreRule? {
        return advancedRules.firstOrNull { rule ->
            rule.matches(description, amount)
        }
    }
    
    companion object {
        fun default() = IgnorePatterns()
    }
}
