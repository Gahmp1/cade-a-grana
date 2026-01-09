package com.financehub.domain.model

import kotlinx.serialization.Serializable

/**
 * Categorias de transações
 */
@Serializable
enum class Category(val displayName: String, val keywords: List<String>) {
    ALIMENTACAO(
        "Alimentação",
        listOf(
            "ifood", "rappi", "uber eats", "restaurante", "lanchonete", 
            "padaria", "mercado", "supermercado", "açougue", "hortifruti"
        )
    ),
    
    TRANSPORTE(
        "Transporte",
        listOf(
            "uber", "99", "taxi", "combustivel", "gasolina", "etanol",
            "estacionamento", "pedagio", "metro", "onibus"
        )
    ),
    
    SAUDE(
        "Saúde",
        listOf(
            "farmacia", "drogaria", "hospital", "clinica", "medico",
            "dentista", "laboratorio", "exame", "consulta"
        )
    ),
    
    LAZER(
        "Lazer",
        listOf(
            "cinema", "streaming", "netflix", "spotify", "youtube",
            "prime", "disney", "hbo", "entretenimento", "show"
        )
    ),
    
    EDUCACAO(
        "Educação",
        listOf(
            "curso", "faculdade", "escola", "livro", "educacao",
            "udemy", "coursera", "alura"
        )
    ),
    
    COMPRAS(
        "Compras",
        listOf(
            "mercado livre", "amazon", "shopee", "magalu", "americanas",
            "netshoes", "zara", "renner", "loja", "compra"
        )
    ),
    
    CONTAS(
        "Contas e Serviços",
        listOf(
            "energia", "agua", "internet", "telefone", "celular",
            "aluguel", "condominio", "gas", "luz", "netflix"
        )
    ),
    
    INVESTIMENTOS(
        "Investimentos",
        listOf(
            "investimento", "aplicacao", "renda fixa", "cdb", "tesouro",
            "acao", "fundo", "previdencia", "poupanca"
        )
    ),
    
    TRANSFERENCIAS(
        "Transferências",
        listOf(
            "transferencia", "pix", "ted", "doc", "pagamento", "p2p"
        )
    ),
    
    SALARIO(
        "Salário e Renda",
        listOf(
            "salario", "pagamento", "vencimento", "remuneracao",
            "provento", "credito salarial"
        )
    ),
    
    OUTROS(
        "Outros",
        listOf()
    );
    
    companion object {
        /**
         * Classifica uma transação baseada na descrição
         */
        fun classify(description: String): Category {
            val desc = description.lowercase()
            
            // Primeira passagem: verificar keywords específicas
            values().forEach { category ->
                if (category != OUTROS) {
                    category.keywords.forEach { keyword ->
                        if (desc.contains(keyword)) {
                            return category
                        }
                    }
                }
            }
            
            return OUTROS
        }
    }
}
