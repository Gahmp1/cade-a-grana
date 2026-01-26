package com.financehub.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.foundation.TextFieldVariant
import com.financehub.designsystem.foundation.toColors
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSRadius
import com.financehub.designsystem.tokens.DSTypography

/**
 * Design System - TextField Component
 * 
 * Campo de texto unificado com variantes semânticas.
 * Suporta ícones, placeholder, labels e validação.
 * 
 * @param value Valor atual do campo
 * @param onValueChange Callback quando o valor muda
 * @param modifier Modifier do Compose
 * @param variant Variante semântica (DEFAULT, SEARCH, ERROR)
 * @param placeholder Texto de placeholder
 * @param label Label do campo
 * @param leadingIcon Ícone à esquerda
 * @param trailingIcon Ícone à direita
 * @param enabled Se o campo está habilitado
 * @param readOnly Se o campo é somente leitura
 * @param singleLine Se o campo é de linha única
 * @param maxLines Número máximo de linhas
 * @param keyboardType Tipo de teclado
 * @param imeAction Ação do IME
 * @param onImeAction Callback quando ação do IME é acionada
 * @param isError Se o campo está em estado de erro
 * @param errorMessage Mensagem de erro
 * 
 * @example
 * ```kotlin
 * // Campo simples
 * DSTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     placeholder = "Digite aqui"
 * )
 * 
 * // Campo de busca
 * DSTextField(
 *     value = search,
 *     onValueChange = { search = it },
 *     variant = TextFieldVariant.SEARCH,
 *     placeholder = "Buscar transações..."
 * )
 * 
 * // Campo com erro
 * DSTextField(
 *     value = email,
 *     onValueChange = { email = it },
 *     isError = !isValidEmail,
 *     errorMessage = "Email inválido"
 * )
 * ```
 */
@Composable
fun DSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: TextFieldVariant = TextFieldVariant.DEFAULT,
    placeholder: String? = null,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val effectiveVariant = when {
        isError -> TextFieldVariant.ERROR
        !enabled -> TextFieldVariant.DISABLED
        else -> variant
    }
    val colors = effectiveVariant.toColors()
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = DSTypography.bodyMedium,
        label = label?.let { { Text(it, style = DSTypography.labelMedium) } },
        placeholder = placeholder?.let { 
            { 
                Text(
                    text = it, 
                    style = DSTypography.bodyMedium,
                    color = DSColors.Dark.textTertiary
                ) 
            } 
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = DSColors.Dark.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                if (onTrailingIconClick != null) {
                    IconButton(onClick = onTrailingIconClick) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = DSColors.Dark.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = DSColors.Dark.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = DSColors.Expense.main, style = DSTypography.labelSmall) }
        } else null,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction?.invoke() },
            onSearch = { onImeAction?.invoke() },
            onGo = { onImeAction?.invoke() }
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = DSRadius.sm,
        colors = colors
    )
}

/**
 * Campo de busca especializado
 * 
 * @example
 * ```kotlin
 * DSSearchField(
 *     value = query,
 *     onValueChange = { query = it },
 *     placeholder = "Buscar transações...",
 *     onClear = { query = "" }
 * )
 * ```
 */
@Composable
fun DSSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    DSTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        variant = TextFieldVariant.SEARCH,
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        trailingIcon = if (value.isNotEmpty()) Icons.Default.Clear else null,
        onTrailingIconClick = onClear,
        imeAction = ImeAction.Search,
        onImeAction = onSearch
    )
}
