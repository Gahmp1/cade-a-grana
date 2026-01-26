@file:Suppress("unused")

package com.financehub.designsystem

/**
 * FinanceHub Design System
 * 
 * Sistema de design unificado com componentes reutilizáveis, tokens semânticos
 * e style presets para toda a aplicação.
 * 
 * ## Quick Start
 * 
 * ```kotlin
 * import com.financehub.designsystem.components.*
 * import com.financehub.designsystem.foundation.*
 * import com.financehub.designsystem.tokens.*
 * import com.financehub.designsystem.styles.*
 * ```
 * 
 * ## Componentes Disponíveis
 * 
 * - `DSText` - Texto com variantes semânticas
 * - `DSMoneyText` - Valores monetários formatados
 * - `DSButton` - Botão com variantes (PRIMARY, SECONDARY, POSITIVE, NEGATIVE, GHOST)
 * - `DSTextButton` - Botão simplificado apenas com texto
 * - `DSIconButton` - Botão de ícone
 * - `DSCard` - Card com slots (header, content, footer)
 * - `DSCardCompact` - Card compacto sem slots
 * - `DSErrorCard` - Card de erro pré-estilizado
 * - `DSTextField` - Campo de texto
 * - `DSSearchField` - Campo de busca
 * - `DSBadge` - Badge/chip
 * - `DSCategoryBadge` - Badge de categoria
 * - `DSOwnerBadge` - Badge de membro
 * - `DSStatusBadge` - Badge de status
 * - `DSSelector` - Dropdown selector
 * - `DSNullableSelector` - Dropdown com opção null
 * - `DSCheckbox` - Checkbox
 * - `DSRadioButton` - RadioButton
 * - `DSRadioGroup` - Grupo de RadioButtons
 * 
 * ## Tokens
 * 
 * - `DSSpacing` - Espaçamentos (none, xs, sm, md, lg, xl, xxl)
 * - `DSRadius` - Border radius (none, xs, sm, md, lg, xl, xxl, full)
 * - `DSElevation` - Elevações (none, xs, sm, md, lg, xl, xxl)
 * - `DSDuration` - Durações de animação (instant, fast, normal, medium, slow)
 * - `DSColors` - Paleta de cores
 * - `DSTypography` - Escala tipográfica
 * 
 * ## Variants
 * 
 * - `ButtonVariant` - PRIMARY, SECONDARY, POSITIVE, NEGATIVE, GHOST, WARNING
 * - `TextVariant` - DISPLAY, HEADLINE, TITLE, BODY, CAPTION, LABEL, INCOME, EXPENSE
 * - `CardVariant` - DEFAULT, SELECTED, ELEVATED, SUCCESS, ERROR, WARNING, MUTED, OUTLINED
 * - `TextFieldVariant` - DEFAULT, SEARCH, ERROR, DISABLED
 * - `BadgeVariant` - DEFAULT, CATEGORY, OWNER, SUCCESS, ERROR, INFO, WARNING
 * - `ComponentSize` - XS, SM, MD, LG, XL
 * 
 * ## Style Presets (Modifier Extensions)
 * 
 * - `Modifier.tipsCardStyle()` - Card de dicas
 * - `Modifier.summaryCardStyle()` - Card de resumo
 * - `Modifier.transactionCardStyle()` - Card de transação
 * - `Modifier.sectionStyle()` - Seção de conteúdo
 * - `Modifier.pageContainerStyle()` - Container de página
 * - `Modifier.listItemStyle()` - Item de lista
 * - `Modifier.dialogContainerStyle()` - Container de dialog
 */
