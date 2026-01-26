package com.financehub.designsystem.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.financehub.designsystem.tokens.DSColors
import com.financehub.designsystem.tokens.DSComponentSpacing
import com.financehub.designsystem.tokens.DSRadius
import com.financehub.designsystem.tokens.DSSpacing

/**
 * Design System - Modifier Extensions
 * 
 * Style presets como Modifier extensions para padrões de layout reutilizáveis.
 * Seguindo o padrão idiomático do Compose Multiplatform.
 * 
 * @example
 * ```kotlin
 * // Card de dicas
 * Column(modifier = Modifier.tipsCardStyle()) {
 *     Text("💡 Dicas")
 *     tips.forEach { Text("• $it") }
 * }
 * 
 * // Container de seção
 * Column(modifier = Modifier.sectionStyle()) {
 *     // conteúdo
 * }
 * ```
 */

// ============================================
// CARD STYLES
// ============================================

/**
 * Estilo para card de dicas/instruções
 */
fun Modifier.tipsCardStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.lg)
    .background(DSColors.Dark.surface)
    .border(1.dp, DSColors.Dark.borderSubtle, DSRadius.lg)
    .padding(DSSpacing.lg)

/**
 * Estilo para card de resumo financeiro
 */
fun Modifier.summaryCardStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.lg)
    .background(DSColors.Dark.surface)
    .padding(DSSpacing.lg)

/**
 * Estilo para card de transação
 */
fun Modifier.transactionCardStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.md)
    .padding(DSSpacing.md)

/**
 * Estilo para card de erro
 */
fun Modifier.errorCardStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.lg)
    .background(DSColors.Expense.subtle)
    .border(1.dp, DSColors.Expense.dark, DSRadius.lg)
    .padding(DSSpacing.lg)

/**
 * Estilo para card de sucesso
 */
fun Modifier.successCardStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.lg)
    .background(DSColors.Income.subtle)
    .border(1.dp, DSColors.Income.dark, DSRadius.lg)
    .padding(DSSpacing.lg)

/**
 * Estilo para card destacado (histórico, ações principais)
 */
fun Modifier.highlightedCardStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.lg)
    .background(DSColors.Income.main)
    .padding(DSSpacing.lg)

// ============================================
// LAYOUT STYLES
// ============================================

/**
 * Estilo para seção de conteúdo
 */
fun Modifier.sectionStyle(): Modifier = this
    .fillMaxWidth()
    .padding(vertical = DSSpacing.md)

/**
 * Estilo para container principal (com padding de página)
 */
fun Modifier.pageContainerStyle(): Modifier = this
    .fillMaxWidth()
    .padding(DSSpacing.xl)

/**
 * Estilo para sidebar
 */
fun Modifier.sidebarStyle(): Modifier = this
    .width(DSComponentSpacing.sidebarWidth)
    .background(DSColors.Dark.surface)

/**
 * Estilo para toolbar
 */
fun Modifier.toolbarStyle(): Modifier = this
    .fillMaxWidth()
    .height(DSComponentSpacing.toolbarHeight)
    .background(DSColors.Dark.surface)
    .padding(horizontal = DSSpacing.lg)

/**
 * Estilo para área de conteúdo principal
 */
fun Modifier.contentAreaStyle(): Modifier = this
    .fillMaxWidth()
    .padding(DSSpacing.lg)

// ============================================
// FORM STYLES
// ============================================

/**
 * Estilo para grupo de campos de formulário
 */
fun Modifier.formGroupStyle(): Modifier = this
    .fillMaxWidth()
    .padding(vertical = DSSpacing.sm)

/**
 * Estilo para linha de ações (botões)
 */
fun Modifier.actionRowStyle(): Modifier = this
    .fillMaxWidth()
    .padding(top = DSSpacing.lg)

// ============================================
// LIST STYLES
// ============================================

/**
 * Estilo para item de lista
 */
fun Modifier.listItemStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.md)
    .background(DSColors.Dark.surface)
    .padding(DSSpacing.md)

/**
 * Estilo para item de lista selecionado
 */
fun Modifier.listItemSelectedStyle(): Modifier = this
    .fillMaxWidth()
    .clip(DSRadius.md)
    .background(DSColors.Dark.surfaceElevated)
    .border(1.dp, DSColors.Income.main, DSRadius.md)
    .padding(DSSpacing.md)

// ============================================
// BADGE/CHIP STYLES
// ============================================

/**
 * Estilo para badge/chip padrão
 */
fun Modifier.badgeStyle(): Modifier = this
    .clip(DSRadius.xs)
    .background(DSColors.Dark.surfaceVariant)
    .padding(horizontal = DSSpacing.sm, vertical = DSSpacing.xs)

/**
 * Estilo para badge de categoria
 */
fun Modifier.categoryBadgeStyle(): Modifier = this
    .clip(DSRadius.xs)
    .padding(horizontal = DSSpacing.sm, vertical = DSSpacing.xs)

// ============================================
// DIALOG STYLES
// ============================================

/**
 * Estilo para container de dialog/sheet
 */
fun Modifier.dialogContainerStyle(): Modifier = this
    .clip(DSRadius.xl)
    .background(DSColors.Dark.surface)
    .padding(DSSpacing.xl)

/**
 * Estilo para header de dialog
 */
fun Modifier.dialogHeaderStyle(): Modifier = this
    .fillMaxWidth()
    .padding(bottom = DSSpacing.lg)

/**
 * Estilo para footer de dialog (área de botões)
 */
fun Modifier.dialogFooterStyle(): Modifier = this
    .fillMaxWidth()
    .padding(top = DSSpacing.lg)
