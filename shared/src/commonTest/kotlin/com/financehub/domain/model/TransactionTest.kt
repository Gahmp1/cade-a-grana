package com.financehub.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionTest {
    
    @Test
    fun `transaction with positive amount should be income`() {
        val transaction = Transaction(
            id = "1",
            date = LocalDate(2025, 8, 1),
            description = "SALARIO",
            amount = 5000.0,
            category = Category.SALARIO,
            source = DataSource.ITAU_BANK_STATEMENT
        )
        
        assertTrue(transaction.isIncome)
        assertEquals(5000.0, transaction.absAmount)
    }
    
    @Test
    fun `transaction with negative amount should be expense`() {
        val transaction = Transaction(
            id = "1",
            date = LocalDate(2025, 8, 1),
            description = "IFOOD",
            amount = -50.0,
            category = Category.ALIMENTACAO,
            source = DataSource.ITAU_CREDIT_CARD
        )
        
        assertTrue(transaction.isExpense)
        assertEquals(50.0, transaction.absAmount)
    }
}
