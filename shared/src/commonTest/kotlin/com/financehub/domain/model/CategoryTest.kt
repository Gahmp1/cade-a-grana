package com.financehub.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CategoryTest {
    
    @Test
    fun `classify should identify food transactions`() {
        val category = Category.classify("IFOOD DELIVERY")
        assertEquals(Category.ALIMENTACAO, category)
    }
    
    @Test
    fun `classify should identify transport transactions`() {
        val category = Category.classify("UBER VIAGEM")
        assertEquals(Category.TRANSPORTE, category)
    }
    
    @Test
    fun `classify should be case insensitive`() {
        val category1 = Category.classify("netflix")
        val category2 = Category.classify("NETFLIX")
        val category3 = Category.classify("NetFlix")
        
        assertEquals(Category.LAZER, category1)
        assertEquals(Category.LAZER, category2)
        assertEquals(Category.LAZER, category3)
    }
    
    @Test
    fun `classify should return OUTROS for unknown descriptions`() {
        val category = Category.classify("SOMETHING COMPLETELY UNKNOWN")
        assertEquals(Category.OUTROS, category)
    }
    
    @Test
    fun `all categories should have display names`() {
        Category.values().forEach { category ->
            assertTrue(category.displayName.isNotEmpty())
        }
    }
}
