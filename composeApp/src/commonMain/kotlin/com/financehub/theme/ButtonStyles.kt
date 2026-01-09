package com.financehub.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Estilos reutilizáveis de botões seguindo o Design System
 * ⚠️ SEMPRE use essas funções ao invés de criar cores inline
 */
object FinanceButtonStyles {
    
    /**
     * Estilo padrão para botões primários (Button)
     * Texto branco em fundo preto
     */
    @Composable
    fun primaryButtonColors(): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
    
    /**
     * Estilo para botões secundários (OutlinedButton)
     * Texto branco com borda branca
     */
    @Composable
    fun outlinedButtonColors(): ButtonColors {
        return ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
    /**
     * Estilo para botões de ação positiva (confirmar, salvar)
     */
    @Composable
    fun positiveButtonColors(): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary, // Verde
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    }
    
    /**
     * Estilo para botões de ação negativa (cancelar, deletar)
     */
    @Composable
    fun negativeButtonColors(): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error, // Vermelho
            contentColor = MaterialTheme.colorScheme.onError
        )
    }
    
    /**
     * Estilo para outlined button de ação negativa
     */
    @Composable
    fun outlinedNegativeButtonColors(): ButtonColors {
        return ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}
