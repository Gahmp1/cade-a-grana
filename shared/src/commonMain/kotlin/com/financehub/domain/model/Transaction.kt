package com.financehub.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

/**
 * Representa uma transação financeira
 */
@Serializable
data class Transaction(
    val id: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val description: String,
    val amount: Double, // Positivo = Entrada, Negativo = Saída
    val category: Category,
    val source: DataSource,
    val rawData: String = "",
    val isIgnored: Boolean = false, // Transações ignoradas não entram em cálculos
    val needsReview: Boolean = false, // Marcada para revisão (regra avançada)
    val customSourceTag: String? = null // Tag customizada pelo usuário para o banco/fonte
) {
    val isIncome: Boolean get() = amount > 0
    val isExpense: Boolean get() = amount < 0
    val absAmount: Double get() = kotlin.math.abs(amount)
    
    /**
     * Nome efetivo da fonte: usa tag customizada se disponível, senão usa displayName do DataSource
     */
    val effectiveSourceName: String get() = customSourceTag ?: source.displayName
}
