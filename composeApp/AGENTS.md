# FinanceHub Design System Agent

Agente especializado no **Design System** do FinanceHub. Use este agente ao criar ou modificar componentes de UI.

> **Referências:** Apple HIG + Material 3 Tokens + Tailwind Spacing

## Design System Overview

O FinanceHub usa um Design System customizado com componentes genéricos prefixados com `DS`, tokens semânticos e style presets. Foco em **reutilização**, **simplicidade** e **modernidade**.

### Estrutura do Design System

```
composeApp/src/commonMain/kotlin/com/financehub/
├── designsystem/
│   ├── components/          # Componentes DS (DSButton, DSCard, etc.)
│   │   ├── DSText.kt
│   │   ├── DSButton.kt
│   │   ├── DSCard.kt
│   │   ├── DSTextField.kt
│   │   ├── DSBadge.kt
│   │   ├── DSSelector.kt
│   │   ├── DSCheckbox.kt
│   │   └── DSRadioButton.kt
│   ├── foundation/          # Variants e configurações
│   │   └── Variants.kt      # ButtonVariant, TextVariant, CardVariant, etc.
│   ├── tokens/              # Design tokens
│   │   ├── Spacing.kt       # DSSpacing (grid 4pt)
│   │   ├── Radius.kt        # DSRadius
│   │   ├── Elevation.kt     # DSElevation
│   │   ├── Duration.kt      # DSDuration (animações)
│   │   ├── Colors.kt        # DSColors
│   │   └── Typography.kt    # DSTypography
│   ├── styles/              # Style presets (Modifier extensions)
│   │   └── Modifiers.kt     # tipsCardStyle(), summaryCardStyle(), etc.
│   └── DesignSystem.kt      # Documentação e re-exports
└── theme/
    └── Theme.kt             # FinanceHubTheme (usa DSColors)
```

## ⚠️ CRITICAL RULES

### ✅ ALWAYS DO

```kotlin
// 1. Use componentes DS em vez de Material direto
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*

// 2. Use DSText com variants semânticos
DSText("Título da Página", variant = TextVariant.HEADLINE)
DSText("Conteúdo", variant = TextVariant.BODY)
DSMoneyText(amount = 1500.00, isIncome = true)  // Formatação automática

// 3. Use DSButton com variants
DSButton(onClick = { }, variant = ButtonVariant.PRIMARY) { Text("Ação") }
DSButton(onClick = { }, variant = ButtonVariant.POSITIVE) { Text("Confirmar") }
DSButton(onClick = { }, variant = ButtonVariant.NEGATIVE) { Text("Excluir") }

// 4. Use DSCard com slots
DSCard(
    variant = CardVariant.DEFAULT,
    header = { DSText("Título", variant = TextVariant.TITLE) },
    footer = { DSButton(...) }
) {
    DSText("Conteúdo do card")
}

// 5. Use tokens de espaçamento
Modifier.padding(DSSpacing.lg)  // 16.dp
Arrangement.spacedBy(DSSpacing.sm)  // 8.dp

// 6. Use style presets para padrões complexos
Column(modifier = Modifier.tipsCardStyle()) {
    DSText("💡 Dicas", variant = TextVariant.TITLE)
}
```

### ❌ NEVER DO

```kotlin
// ❌ NEVER use Material components directly (prefira DS)
Button(...) { }  // Use DSButton
Text(...) { }    // Use DSText
Card(...) { }    // Use DSCard

// ❌ NEVER hardcode colors
Text(color = Color(0xFF00C853))  // Use DSColors.Income.main
Text(color = Color.Red)          // Use DSColors.Expense.main

// ❌ NEVER hardcode spacing
Modifier.padding(16.dp)  // Use DSSpacing.lg

// ❌ NEVER create custom color values
val myGreen = Color(0xFF4CAF50)  // Use DSColors
```

## Componentes DS

### DSText

Texto com variantes semânticas e formatação automática para valores monetários.

```kotlin
// Variantes disponíveis
enum class TextVariant {
    DISPLAY,      // Título de página principal (32sp)
    HEADLINE,     // Título de seção (20sp)
    TITLE,        // Título de card (16sp)
    BODY,         // Texto de conteúdo (13sp)
    CAPTION,      // Texto secundário (12sp)
    LABEL,        // Rótulo pequeno (11sp)
    INCOME,       // Valor de entrada (verde, bold)
    EXPENSE,      // Valor de saída (vermelho, bold)
    NEUTRAL,      // Valor neutro (cinza, bold)
    MONEY_LARGE,  // Valor grande destaque (20sp)
    MONEY,        // Valor monetário padrão (16sp)
    MONEY_SMALL   // Valor monetário pequeno (13sp)
}

// Exemplos
DSText("Relatório Mensal", variant = TextVariant.HEADLINE)
DSText("Descrição do item", variant = TextVariant.BODY)
DSText("R$ 1.500,00", variant = TextVariant.INCOME)

// Para valores monetários com formatação automática
DSMoneyText(amount = 1500.00, isIncome = true)
DSMoneyText(amount = -500.00, isIncome = false, size = MoneyTextSize.LARGE)
```

### DSButton

Botão com variantes semânticas, tamanhos e suporte a ícones.

```kotlin
// Variantes disponíveis
enum class ButtonVariant {
    PRIMARY,    // Ação principal (fundo escuro)
    SECONDARY,  // Ação secundária (outlined)
    POSITIVE,   // Confirmar, salvar (verde)
    NEGATIVE,   // Excluir, cancelar (vermelho)
    GHOST,      // Mínimo peso visual
    WARNING     // Avisos (amarelo)
}

// Tamanhos
enum class ComponentSize { XS, SM, MD, LG, XL }

// Exemplos
DSButton(onClick = { }) { Text("Ação") }

DSButton(
    onClick = { },
    variant = ButtonVariant.POSITIVE,
    leadingIcon = Icons.Default.Check
) { Text("Confirmar") }

DSTextButton(
    text = "Excluir",
    onClick = { },
    variant = ButtonVariant.NEGATIVE
)

DSIconButton(
    icon = Icons.Default.Close,
    onClick = { },
    contentDescription = "Fechar"
)
```

### DSCard

Card com slots opcionais (header, content, footer) e variantes semânticas.

```kotlin
// Variantes disponíveis
enum class CardVariant {
    DEFAULT,    // Card padrão
    SELECTED,   // Card ativo/selecionado
    ELEVATED,   // Card com destaque
    SUCCESS,    // Contexto positivo (verde sutil)
    ERROR,      // Contexto negativo (vermelho sutil)
    WARNING,    // Contexto de aviso (amarelo sutil)
    MUTED,      // Desabilitado/ignorado
    OUTLINED    // Apenas borda
}

// Card com slots
DSCard(
    variant = CardVariant.DEFAULT,
    header = { 
        DSText("Resumo", variant = TextVariant.TITLE) 
    },
    footer = {
        Row {
            DSButton(onClick = { }) { Text("Cancelar") }
            DSButton(onClick = { }, variant = ButtonVariant.POSITIVE) { 
                Text("Salvar") 
            }
        }
    }
) {
    DSText("Conteúdo do card")
    DSText("Mais conteúdo")
}

// Card compacto (sem slots)
DSCardCompact(
    variant = CardVariant.SUCCESS,
    onClick = { }
) {
    Row {
        DSText("Transação")
        Spacer(Modifier.weight(1f))
        DSMoneyText(amount = 100.0, isIncome = true)
    }
}

// Card de erro pré-estilizado
DSErrorCard(message = "Erro ao processar arquivo")
```

### DSTextField

Campo de texto com variantes e suporte a validação.

```kotlin
// Variantes
enum class TextFieldVariant { DEFAULT, SEARCH, ERROR, DISABLED }

// Campo padrão
DSTextField(
    value = text,
    onValueChange = { text = it },
    placeholder = "Digite aqui",
    label = "Nome"
)

// Campo de busca (com ícones automáticos)
DSSearchField(
    value = query,
    onValueChange = { query = it },
    placeholder = "Buscar transações...",
    onClear = { query = "" }
)

// Campo com erro
DSTextField(
    value = email,
    onValueChange = { email = it },
    isError = !isValidEmail,
    errorMessage = "Email inválido"
)
```

### DSBadge

Badge/chip para labels, categorias, status.

```kotlin
// Variantes
enum class BadgeVariant { DEFAULT, CATEGORY, OWNER, SUCCESS, ERROR, INFO, WARNING }

// Badge padrão
DSBadge(text = "Novo")

// Badge de categoria (cor automática baseada no nome)
DSCategoryBadge(categoryName = "Alimentação")

// Badge de membro (cor customizada)
DSOwnerBadge(ownerName = "João", ownerColor = Color.Blue)

// Badge de status
DSStatusBadge(text = "Pendente", variant = BadgeVariant.WARNING)
```

### DSSelector

Dropdown selector tipado.

```kotlin
// Selector simples
DSSelector(
    options = listOf("Opção 1", "Opção 2", "Opção 3"),
    selectedOption = selected,
    onOptionSelected = { selected = it },
    placeholder = "Selecione..."
)

// Selector tipado com displayText
DSSelector(
    options = categories,
    selectedOption = selectedCategory,
    onOptionSelected = { selectedCategory = it },
    displayText = { it.displayName }
)

// Selector com opção null
DSNullableSelector(
    options = members,
    selectedOption = selectedMember,
    onOptionSelected = { selectedMember = it },
    nullOptionText = "Não atribuído"
)
```

### DSCheckbox e DSRadioButton

```kotlin
// Checkbox com label
DSCheckbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it },
    label = "Lembrar-me"
)

// RadioButton
DSRadioButton(
    selected = selectedOption == "A",
    onClick = { selectedOption = "A" },
    label = "Opção A"
)

// RadioGroup
DSRadioGroup(
    options = listOf("Diário", "Semanal", "Mensal"),
    selectedOption = frequency,
    onOptionSelected = { frequency = it },
    horizontal = true
)
```
## Tokens

### DSSpacing (Grid 4pt - Tailwind inspired)

```kotlin
object DSSpacing {
    val none = 0.dp      // Sem espaçamento
    val xxs = 2.dp       // Micro (ícone-texto inline)
    val xs = 4.dp        // Extra pequeno
    val sm = 8.dp        // Pequeno (elementos relacionados)
    val md = 12.dp       // Médio-pequeno (badges)
    val lg = 16.dp       // Médio (padding padrão) ⭐
    val xl = 20.dp       // Médio-grande
    val xxl = 24.dp      // Grande (seções)
    val xxxl = 32.dp     // Extra grande (margens de página)
    val jumbo = 48.dp    // Áreas principais
    val super_ = 64.dp   // Toolbar height
}

// Uso
Modifier.padding(DSSpacing.lg)
Arrangement.spacedBy(DSSpacing.sm)
```

### DSRadius (macOS-style corners)

```kotlin
object DSRadius {
    val none = RoundedCornerShape(0.dp)
    val xs = RoundedCornerShape(4.dp)    // Badges
    val sm = RoundedCornerShape(6.dp)    // Botões, inputs
    val md = RoundedCornerShape(8.dp)    // Cards padrão
    val lg = RoundedCornerShape(12.dp)   // Cards destacados ⭐
    val xl = RoundedCornerShape(16.dp)   // Modais
    val xxl = RoundedCornerShape(24.dp)  // Hero cards
    val full = RoundedCornerShape(50%)   // Circular
}

// Uso
Card(shape = DSRadius.lg)
Modifier.clip(DSRadius.md)
```

### DSColors

```kotlin
object DSColors {
    // Cores semânticas de finanças
    object Income {
        val main = Color(0xFF00C853)    // Verde
        val light = Color(0xFF69F0AE)
        val dark = Color(0xFF00A040)
        val subtle = main.copy(alpha = 0.1f)
    }
    
    object Expense {
        val main = Color(0xFFFF1744)    // Vermelho
        val light = Color(0xFFFF5252)
        val dark = Color(0xFFD50000)
        val subtle = main.copy(alpha = 0.1f)
    }
    
    object Warning { ... }
    object Info { ... }
    object Neutral { ... }
    
    // Superfícies (Dark Mode)
    object Dark {
        val background = Black
        val surface = Gray900
        val surfaceVariant = Gray850
        val surfaceElevated = Gray800
        val border = Gray700
        val textPrimary = White
        val textSecondary = Gray400
    }
    
    // Utilitários
    fun getCategoryColor(index: Int): Color
    fun parseHex(hex: String): Color
}
```

### DSTypography

```kotlin
object DSTypography {
    // Display (títulos de página)
    val displayLarge   // 32sp, Bold
    val displayMedium  // 28sp, SemiBold
    val displaySmall   // 24sp, SemiBold
    
    // Headline (títulos de seção)
    val headlineLarge  // 22sp, SemiBold
    val headlineMedium // 20sp, SemiBold
    val headlineSmall  // 18sp, Medium
    
    // Title (cards/componentes)
    val titleLarge     // 16sp, SemiBold
    val titleMedium    // 14sp, Medium
    val titleSmall     // 13sp, Medium
    
    // Body (conteúdo)
    val bodyLarge      // 14sp
    val bodyMedium     // 13sp ⭐
    val bodySmall      // 12sp
    
    // Label (rótulos)
    val labelLarge     // 12sp, Medium
    val labelMedium    // 11sp, Medium
    val labelSmall     // 10sp, Medium
    
    // Especiais (valores monetários)
    val money          // 16sp, Bold
    val moneyLarge     // 20sp, Bold
    val moneySmall     // 13sp, SemiBold
}
```

## Style Presets (Modifier Extensions)

Para padrões de layout complexos, use style presets:

```kotlin
import com.financehub.designsystem.styles.*

// Card de dicas/instruções
Column(modifier = Modifier.tipsCardStyle()) {
    DSText("💡 Dicas", variant = TextVariant.TITLE)
    tips.forEach { DSText("• $it") }
}

// Card de resumo financeiro
Column(modifier = Modifier.summaryCardStyle()) { ... }

// Container de página
Column(modifier = Modifier.pageContainerStyle()) { ... }

// Item de lista
Row(modifier = Modifier.listItemStyle()) { ... }
Row(modifier = Modifier.listItemSelectedStyle()) { ... }

// Toolbar
Row(modifier = Modifier.toolbarStyle()) { ... }

// Dialog/Sheet
Column(modifier = Modifier.dialogContainerStyle()) {
    Row(modifier = Modifier.dialogHeaderStyle()) { ... }
    // content
    Row(modifier = Modifier.dialogFooterStyle()) { ... }
}
```

## Exemplos de Uso

### Botões
```kotlin
DSButton(onClick = { }, variant = ButtonVariant.PRIMARY) { Text("Primário") }
DSButton(onClick = { }, variant = ButtonVariant.POSITIVE) { Text("Confirmar") }
DSButton(onClick = { }, variant = ButtonVariant.NEGATIVE) { Text("Excluir") }
```

### Valores Monetários
```kotlin
DSMoneyText(amount = 1500.00, isIncome = true)   // Verde
DSMoneyText(amount = 350.00, isIncome = false)    // Vermelho
```

### Espaçamento
```kotlin
Modifier.padding(DSSpacing.lg)  // 16.dp
Arrangement.spacedBy(DSSpacing.sm)  // 8.dp
```

## Anti-Patterns Checklist

Antes de submeter código, verifique:

- [ ] Sem `Color(0xFF...)` hardcoded → use `DSColors`
- [ ] Sem `16.dp`, `8.dp` hardcoded → use `DSSpacing.lg`, `DSSpacing.sm`
- [ ] Sem `Button()` direto → use `DSButton`
- [ ] Sem `Card()` direto → use `DSCard` ou `DSCardCompact`
- [ ] Sem `Text()` direto em contextos semânticos → use `DSText` com variant
- [ ] Valores monetários usam `DSMoneyText`
- [ ] Categorias usam `DSCategoryBadge`
- [ ] Owners usam `DSOwnerBadge`

---

**O Design System garante consistência visual. Quando em dúvida, verifique os componentes em `designsystem/components/`.**
