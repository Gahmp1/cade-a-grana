package com.financehub.domain.model

import kotlinx.serialization.Serializable

/**
 * Origem dos dados da transação.
 * Extensível para novos bancos e fontes de dados.
 */
@Serializable
enum class DataSource(val displayName: String) {
    // Itaú
    ITAU_CREDIT_CARD("Cartão Itaú"),
    ITAU_BANK_STATEMENT("Extrato Itaú"),
    
    // PicPay
    PICPAY_STATEMENT("Extrato PicPay"),
    
    // Nubank
    NUBANK_CREDIT_CARD("Cartão Nubank"),
    NUBANK_BANK_STATEMENT("Extrato Nubank"),
    
    // Mercado Pago
    MERCADOPAGO_STATEMENT("Extrato Mercado Pago"),
    
    // Inter
    INTER_BANK_STATEMENT("Extrato Inter"),
    
    // C6
    C6_BANK_STATEMENT("Extrato C6"),
    
    // Bradesco
    BRADESCO_CREDIT_CARD("Cartão Bradesco"),
    BRADESCO_BANK_STATEMENT("Extrato Bradesco"),
    
    // Santander
    SANTANDER_CREDIT_CARD("Cartão Santander"),
    SANTANDER_BANK_STATEMENT("Extrato Santander"),
    
    // Caixa Econômica Federal
    CAIXA_BANK_STATEMENT("Extrato Caixa"),
    
    // Banco do Brasil
    BANCO_BRASIL_STATEMENT("Extrato Banco do Brasil"),
    
    // Genérico
    MANUAL("Manual"),
    UNKNOWN("Desconhecido");
    
    companion object {
        /**
         * Retorna bancos brasileiros suportados
         */
        fun supportedBanks(): List<String> = listOf(
            "Itaú", "PicPay", "Nubank", "Mercado Pago", "Inter", 
            "C6", "Bradesco", "Santander", "Caixa", "Banco do Brasil"
        )
    }
}
