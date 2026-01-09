package com.financehub.domain.model

import kotlinx.serialization.Serializable

/**
 * Origem dos dados da transação
 */
@Serializable
enum class DataSource(val displayName: String) {
    ITAU_CREDIT_CARD("Cartão Itaú"),
    ITAU_BANK_STATEMENT("Extrato Itaú"),
    PICPAY_STATEMENT("Extrato PicPay"),
    MANUAL("Manual");
}
