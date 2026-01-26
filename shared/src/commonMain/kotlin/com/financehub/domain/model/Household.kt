package com.financehub.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa a configuração da "Casa" - visão conjunta de finanças
 */
@Serializable
data class Household(
    val name: String = "Casa",
    val members: List<HouseholdMember> = listOf(
        HouseholdMember("João", "#2196F3"),
        HouseholdMember("Daiane", "#E91E63")
    ),
    val sharedLabel: String = "Compartilhado"
) {
    /**
     * Obtém membro por nome
     */
    fun getMember(name: String?): HouseholdMember? = members.find { it.name == name }
    
    /**
     * Lista de opções de owner incluindo compartilhado
     */
    val ownerOptions: List<String> get() = members.map { it.name } + sharedLabel
}

/**
 * Representa um membro da casa
 */
@Serializable
data class HouseholdMember(
    val name: String,
    val colorHex: String, // Cor para identificação visual
    val isActive: Boolean = true
)

/**
 * Resumo financeiro por membro
 */
data class MemberSummary(
    val member: HouseholdMember?,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val transactionCount: Int,
    val categories: Map<Category, Double> // Gastos por categoria
) {
    val isShared: Boolean get() = member == null
}
