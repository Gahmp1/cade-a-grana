package com.financehub.data.parser.common

import kotlinx.datetime.LocalDate

/**
 * Padrões e utilitários para parsing de documentos financeiros brasileiros.
 * Centraliza regex e funções de conversão comuns a todos os bancos.
 */
object BrazilianPatterns {
    
    // ==================== PADRÕES DE STRING (para composição de regex) ====================
    
    /** Padrão de data BR como string: dd/MM/yyyy */
    const val DATE_BR_PATTERN = """(\d{2}/\d{2}/\d{4})"""
    
    /** Padrão de data curta como string: dd/MM */
    const val DATE_SHORT_PATTERN = """(\d{2}/\d{2})"""
    
    /** Padrão de valor BR como string: 1.234,56 ou -1.234,56 */
    const val VALUE_BR_PATTERN = """(-?\d{1,3}(?:\.\d{3})*,\d{2})"""
    
    /** Padrão de valor flexível como string: aceita BR ou decimal */
    const val VALUE_ANY_PATTERN = """(-?\d{1,3}(?:[.,]\d{3})*[.,]\d{2}|-?\d+[.,]\d{2})"""
    
    /** Padrão de valor com símbolo R$ como string */
    const val VALUE_CURRENCY_PATTERN = """R\$\s*(\d{1,3}(?:\.\d{3})*,\d{2})"""
    
    // ==================== REGEX DE DATA ====================
    
    /** Data no formato brasileiro: dd/MM/yyyy */
    val DATE_BR = DATE_BR_PATTERN.toRegex()
    
    /** Data no formato ISO: yyyy-MM-dd */
    val DATE_ISO = """(\d{4}-\d{2}-\d{2})""".toRegex()
    
    /** Data com hora: dd/MM/yyyy HH:mm:ss */
    val DATETIME_BR = """(\d{2}/\d{2}/\d{4})\s+(\d{2}:\d{2}:\d{2})""".toRegex()
    
    // ==================== REGEX DE VALOR ====================
    
    /** Valor monetário brasileiro: R$ 1.234,56 */
    val VALUE_BR_CURRENCY = VALUE_CURRENCY_PATTERN.toRegex()
    
    /** Valor monetário sem símbolo R$, formato BR: 1.234,56 ou -1.234,56 */
    val VALUE_BR = VALUE_BR_PATTERN.toRegex()
    
    /** Valor decimal simples: 1234.56 ou -1234.56 */
    val VALUE_DECIMAL = """(-?\d+(?:\.\d+)?)""".toRegex()
    
    /** Valor em qualquer formato: aceita BR (1.234,56) ou decimal (-123.45) */
    val VALUE_ANY = VALUE_ANY_PATTERN.toRegex()
    
    /** Hora no formato HH:MM:SS */
    val TIME_PATTERN = """(\d{2}:\d{2}:\d{2})""".toRegex()
    
    // ==================== BUILDERS DE REGEX ====================
    
    /**
     * Cria regex para linha: DATA + DESCRIÇÃO + VALOR
     * Ex: "02/01/2026 Compra iFood -119.99"
     */
    fun dateDescValueRegex(flexible: Boolean = true): Regex {
        val valuePattern = if (flexible) VALUE_ANY_PATTERN else VALUE_BR_PATTERN
        return """^$DATE_BR_PATTERN\s+(.+?)\s+$valuePattern$""".toRegex()
    }
    
    /**
     * Cria regex para linha: DATA + VALOR + DESCRIÇÃO
     * Ex: "02/01/2026 -119.99 Compra iFood"
     */
    fun dateValueDescRegex(flexible: Boolean = true): Regex {
        val valuePattern = if (flexible) VALUE_ANY_PATTERN else VALUE_BR_PATTERN
        return """^$DATE_BR_PATTERN\s+$valuePattern\s+(.+)$""".toRegex()
    }
    
    /**
     * Cria regex para linha: DATA + DESCRIÇÃO + VALOR + SALDO
     * Ex: "02/01/2026 Compra iFood -119.99 1.234,56"
     */
    fun dateDescValueBalanceRegex(flexible: Boolean = true): Regex {
        val valuePattern = if (flexible) VALUE_ANY_PATTERN else VALUE_BR_PATTERN
        return """^$DATE_BR_PATTERN\s+(.+?)\s+$valuePattern\s+$valuePattern$""".toRegex()
    }
    
    /**
     * Cria regex para linha apenas com data
     * Ex: "02/01/2026"
     */
    fun dateOnlyRegex(): Regex = """^$DATE_BR_PATTERN$""".toRegex()
    
    // ==================== FUNÇÕES DE PARSING DE VALOR ====================
    
    /**
     * Converte valor no formato brasileiro (1.234,56) para Double
     * @param value String com valor no formato BR (pode conter R$)
     * @return Double ou null se inválido
     */
    fun parseValueBR(value: String): Double? {
        return value
            .replace("R$", "")
            .replace(".", "")    // Remove separador de milhar
            .replace(",", ".")   // Vírgula -> ponto decimal
            .trim()
            .toDoubleOrNull()
    }
    
    /**
     * Converte valor decimal simples (1234.56) para Double
     * @param value String com valor decimal
     * @return Double ou null se inválido
     */
    fun parseValueDecimal(value: String): Double? {
        return value.trim().toDoubleOrNull()
    }
    
    /**
     * Detecta automaticamente o formato do valor e converte
     * @param value String com valor monetário
     * @return Double ou null se inválido
     */
    fun parseValueAuto(value: String): Double? {
        val cleaned = value.replace("R$", "").trim()
        
        // Se tem vírgula como decimal (formato BR): 1.234,56
        return if (cleaned.contains(",") && !cleaned.contains(".")) {
            // Formato: 1234,56 (sem separador de milhar)
            cleaned.replace(",", ".").toDoubleOrNull()
        } else if (cleaned.contains(",") && cleaned.contains(".")) {
            // Formato: 1.234,56 (com separador de milhar)
            parseValueBR(cleaned)
        } else {
            // Formato decimal simples: 1234.56
            parseValueDecimal(cleaned)
        }
    }
    
    // ==================== FUNÇÕES DE PARSING DE DATA ====================
    
    /**
     * Converte data no formato brasileiro (dd/MM/yyyy) para LocalDate
     * @param dateStr String no formato dd/MM/yyyy
     * @return LocalDate ou null se inválido
     */
    fun parseDateBR(dateStr: String): LocalDate? {
        return try {
            val parts = dateStr.trim().split("/")
            if (parts.size != 3) return null
            LocalDate(
                year = parts[2].toInt(),
                monthNumber = parts[1].toInt(),
                dayOfMonth = parts[0].toInt()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Converte data no formato ISO (yyyy-MM-dd) para LocalDate
     * @param dateStr String no formato yyyy-MM-dd
     * @return LocalDate ou null se inválido
     */
    fun parseDateISO(dateStr: String): LocalDate? {
        return try {
            val parts = dateStr.trim().split("-")
            if (parts.size != 3) return null
            LocalDate(
                year = parts[0].toInt(),
                monthNumber = parts[1].toInt(),
                dayOfMonth = parts[2].toInt()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Detecta automaticamente o formato da data e converte
     * @param dateStr String com data
     * @return LocalDate ou null se inválido
     */
    fun parseDateAuto(dateStr: String): LocalDate? {
        val trimmed = dateStr.trim()
        return when {
            trimmed.contains("/") -> parseDateBR(trimmed)
            trimmed.contains("-") -> parseDateISO(trimmed)
            else -> null
        }
    }
    
    // ==================== VALIDAÇÃO ====================
    
    /**
     * Verifica se a string é uma data válida no formato brasileiro
     */
    fun isValidDateBR(dateStr: String): Boolean {
        return dateStr.trim().matches(DATE_BR) && parseDateBR(dateStr) != null
    }
    
    /**
     * Verifica se a string é uma data válida no formato ISO
     */
    fun isValidDateISO(dateStr: String): Boolean {
        return dateStr.trim().matches(DATE_ISO) && parseDateISO(dateStr) != null
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Remove hora do início de uma string (ex: "18:17:20 Pix Enviado" -> "Pix Enviado")
     */
    fun removeLeadingTime(text: String): String {
        return text.replace(Regex("^\\d{2}:\\d{2}:\\d{2}\\s*"), "").trim()
    }
    
    /**
     * Extrai o primeiro valor monetário de uma string (apenas formato R$ X.XXX,XX)
     * @return Pair<Double, IntRange> com valor e posição, ou null
     */
    fun extractFirstValue(text: String): Pair<Double, IntRange>? {
        val match = VALUE_BR_CURRENCY.find(text)
        if (match != null) {
            val value = parseValueBR(match.groupValues[1])
            if (value != null) {
                return Pair(value, match.range)
            }
        }
        return null
    }
    
    /**
     * Extrai o primeiro valor monetário de uma string, detectando automaticamente o formato.
     * Suporta: R$ 1.234,56 | 1.234,56 | -1234,56 | 1234.56 | -123.45
     * @return Pair<Double, IntRange> com valor e posição, ou null
     */
    fun extractFirstValueAuto(text: String): Pair<Double, IntRange>? {
        // Primeiro tentar formato com R$
        val matchCurrency = VALUE_BR_CURRENCY.find(text)
        if (matchCurrency != null) {
            val value = parseValueBR(matchCurrency.groupValues[1])
            if (value != null) {
                return Pair(value, matchCurrency.range)
            }
        }
        
        // Tentar formato genérico (BR ou decimal)
        val matchAny = VALUE_ANY.find(text)
        if (matchAny != null) {
            val value = parseValueAuto(matchAny.groupValues[1])
            if (value != null) {
                return Pair(value, matchAny.range)
            }
        }
        
        return null
    }
    
    /**
     * Extrai a primeira data de uma string
     * @return Pair<LocalDate, IntRange> com data e posição, ou null
     */
    fun extractFirstDate(text: String): Pair<LocalDate, IntRange>? {
        // Tentar formato BR primeiro
        val matchBR = DATE_BR.find(text)
        if (matchBR != null) {
            val date = parseDateBR(matchBR.groupValues[1])
            if (date != null) {
                return Pair(date, matchBR.range)
            }
        }
        
        // Tentar formato ISO
        val matchISO = DATE_ISO.find(text)
        if (matchISO != null) {
            val date = parseDateISO(matchISO.groupValues[1])
            if (date != null) {
                return Pair(date, matchISO.range)
            }
        }
        
        return null
    }
}
