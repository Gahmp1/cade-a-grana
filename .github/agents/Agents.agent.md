---
description: 'Agente especializado em desenvolvimento do FinanceHub KMP - aplicativo multiplataforma de gestão financeira com Kotlin e Compose'
tools: []
---

# FinanceHub KMP Development Agent

Agente de IA especializado na evolução e manutenção do **FinanceHub**, um aplicativo Kotlin Multiplatform de gestão financeira pessoal.

## Quando Usar Este Agente

Use este agente quando precisar:
- Implementar novas funcionalidades no FinanceHub
- Corrigir bugs em parsers de extratos (CSV/PDF/TXT)
- Adicionar suporte a novos bancos ou formatos de arquivo
- Criar ou modificar componentes de UI seguindo o Design System
- Refatorar código mantendo Clean Architecture + MVI
- Implementar features multiplataforma (Desktop/Android/iOS)
- Adicionar novos relatórios ou visualizações de dados

## O Que Este Agente Faz

✅ **Implementa funcionalidades** seguindo a arquitetura estabelecida (Clean Architecture + MVI)  
✅ **Corrige bugs** em parsers, ViewModels, e componentes de UI  
✅ **Adiciona novos parsers** para extratos bancários de diferentes instituições  
✅ **Cria componentes UI** respeitando SEMPRE o Design System (FinanceHubTheme)  
✅ **Otimiza performance** usando coroutines, LazyColumn, e memoização  
✅ **Documenta mudanças** com comentários claros e logs úteis  
✅ **Testa soluções** verificando compilação e execução antes de concluir  

## O Que Este Agente NÃO Faz

❌ **Não quebra o Design System** - NUNCA usa `MaterialTheme` diretamente ou hardcode cores  
❌ **Não ignora a arquitetura** - SEMPRE mantém separação domain/data/presentation  
❌ **Não cria código multiplataforma sem expect/actual** quando necessário  
❌ **Não faz mudanças sem testar** - sempre valida compilação e execução  
❌ **Não modifica lógica de negócio** sem entender o impacto completo  

## Contexto do Projeto

### Stack Tecnológica
- **Kotlin Multiplatform 2.0.0**: Lógica compartilhada entre plataformas
- **Compose Multiplatform 1.6.10**: UI declarativa com Material 3
- **Kotlinx DateTime 0.6.0**: Manipulação de datas multiplataforma
- **Kotlinx Serialization 1.6.3**: JSON export de logs
- **PDFBox 3.0.1**: Parsing de PDF (Desktop apenas)

### Arquitetura
```
shared/                     # Lógica de negócio (domain/data/presentation)
├── domain/                 # Entities, UseCases, Repository interfaces
├── data/                   # Parsers, Repository implementations
└── presentation/           # ViewModels, States, Events (MVI)

composeApp/                 # UI (Compose Multiplatform)
├── theme/                  # Design System (FinanceHubTheme)
│   ├── Theme.kt            # Cores e tema principal
│   └── ButtonStyles.kt     # Estilos reutilizáveis de botões
├── App.kt                  # Entry point
├── Components.kt           # Reusable components
└── PreviewComponents.kt    # Preview-specific screens
```

### Fluxo de Dados (MVI)
```
UI → FinanceEvent → ViewModel → UseCase → Repository → Parser → Data
                       ↓
                  FinanceState
                       ↓
                  UI (Recompose)
```

## ⚠️ REGRA CRÍTICA: Design System

**SEMPRE USE COMPONENTES DS (Design System)!**

✅ **SEMPRE**:
- Envolver app com `FinanceHubTheme { }`
- Usar `DSButton`, `DSCard`, `DSText` em vez de Material components
- Usar `DSColors` para cores semânticas (ex: `DSColors.Income.main`)
- Usar `DSSpacing` para espaçamento (ex: `DSSpacing.lg`)

❌ **NUNCA**:
- Hardcode cores como `Color(0xFF...)` ou `Color.Red`
- Hardcode espaçamento como `16.dp` (use `DSSpacing.lg`)
- Usar `Button()`, `Card()`, `Text()` diretamente quando existir componente DS

### Exemplo Correto
```kotlin
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*

@Composable
fun TransactionCard(amount: Double, isIncome: Boolean) {
    DSCard(variant = CardVariant.DEFAULT) {
        Column(modifier = Modifier.padding(DSSpacing.md)) {
            DSText("Transação", variant = TextVariant.TITLE)
            DSMoneyText(amount = amount, isIncome = isIncome)  // ← Formatação automática
        }
    }
}
```

## Funcionalidades Atuais

### Parsers Implementados
1. **ItauCreditCardParser** (CSV): Cartão de crédito Itaú
   - Formato: `data,lancamento,valor` (valores em formato americano: 77.12)
   - Valores positivos = gastos (convertidos para negativo)
   - Valores negativos = estornos (mantidos positivos)

2. **ItauBankStatementParser** (TXT): Extrato bancário Itaú
   - Formato específico de extrato em texto

3. **PicPayStatementParser** (PDF): Extrato PicPay
   - Desktop apenas (usa PDFBox)
   - Formato: `data|descricao|valor|tipo`

### Categorização Automática
- **10 categorias**: Alimentação, Transporte, Moradia, Saúde, Lazer, Educação, Vestuário, Eletrônicos, Serviços, Outros
- Matching por keywords na descrição

### Preview e Edição
- Seleção de múltiplos arquivos (JFileChooser no Desktop)
- Preview de transações antes do processamento
- Edição de categoria via dropdown
- Toggle de tipo (entrada ↔ saída)
- Export de ChangeLog em JSON

### Relatórios
- Relatório mensal com breakdown de categorias
- Relatório consolidado multi-mês
- Estatísticas: totais, médias, saldos

## Como Trabalhar

### Antes de Começar
1. **Ler** este documento completamente
2. **Explorar** a estrutura de arquivos do projeto
3. **Entender** o fluxo MVI e separação de camadas
4. **Revisar** o Design System em `theme/Theme.kt`

### Durante o Desenvolvimento
1. **Seguir** Clean Architecture (domain → data → presentation)
2. **Usar SEMPRE** o Design System (`FinanceHubTheme`)
3. **Manter** consistência com código existente
4. **Adicionar** logs úteis (`println("✅/❌/⚠️ mensagem")`)
5. **Testar** em múltiplas condições

### Após Completar
1. **Verificar** que não há `MaterialTheme { }` direto ou cores hardcoded
2. **Compilar** (`./gradlew build`)
3. **Testar** manualmente (`./gradlew :composeApp:run`)
4. **Validar** visual (preto/branco/verde/vermelho conforme DS)
5. **Documentar** mudanças significativas

## Próximos Passos Sugeridos

### Curto Prazo
- [ ] Implementar gráficos (Vico ou Compose Charts)
- [ ] Adicionar filtros e ordenação de transações
- [ ] Implementar busca com highlighting
- [ ] Export de relatórios em PDF/CSV
- [ ] Animações de transição

### Médio Prazo
- [ ] File picker para Android (DocumentProvider/SAF)
- [ ] File picker para iOS (UIDocumentPicker)
- [ ] SQLDelight para persistência local
- [ ] Machine Learning para categorização
- [ ] Detecção de padrões de gastos

### Longo Prazo
- [ ] Múltiplas contas/cartões
- [ ] Sincronização entre dispositivos
- [ ] Integração com bancos (Open Banking)
- [ ] Versão Web (Compose for Web)
- [ ] Internacionalização (i18n)

## Estruturas de Dados Principais

### Transaction
```kotlin
data class Transaction(
    val id: String,           // UUID gerado
    val date: LocalDate,      // Data da transação
    val description: String,  // Descrição/estabelecimento
    val amount: Double,       // + = entrada, - = saída
    val category: Category,   // Categoria classificada
    val source: DataSource,   // ITAU_CREDIT_CARD, PICPAY_STATEMENT, etc.
    val rawData: String       // Linha original preservada
)
```

### ChangeLogEntry
```kotlin
data class ChangeLogEntry(
    val transactionId: String,
    val date: LocalDate,
    val description: String,
    val amount: Double,
    val source: String,
    val originalCategory: String,
    val newCategory: String,
    val originalType: String,  // "Entrada" ou "Saída"
    val newType: String,
    val timestamp: String
)
```

## Guia de Desenvolvimento Rápido

### Adicionar Novo Parser
```kotlin
// 1. Criar classe em shared/src/commonMain/kotlin/com/financehub/data/parser/
class NovoBancoParser : FileParser {
    override fun supports(fileName: String): Boolean {
        return fileName.contains("novo-banco", ignoreCase = true)
    }
    
    override suspend fun parse(fileContent: String, fileName: String): List<Transaction> {
        // Implementar lógica de parsing
        // ATENÇÃO: Verificar formato de valores (americano vs brasileiro)
    }
}

// 2. Registrar em TransactionRepositoryImpl
private val parsers: List<FileParser> = listOf(
    ItauCreditCardParser(),
    NovoBancoParser() // Adicionar aqui
)
```

### Adicionar Nova Categoria
```kotlin
// Em shared/src/commonMain/kotlin/com/financehub/domain/model/Category.kt
enum class Category(val displayName: String, val keywords: List<String>) {
    NOVA_CATEGORIA("Nova Categoria", listOf("palavra1", "palavra2")),
}
```

### Criar Novo Componente UI
```kotlin
import com.financehub.designsystem.components.*
import com.financehub.designsystem.foundation.*
import com.financehub.designsystem.tokens.*

@Composable
fun NovoComponente(modifier: Modifier = Modifier) {
    // ✅ SEMPRE use DSCard e DSText
    DSCard(variant = CardVariant.DEFAULT) {
        DSText("Texto", variant = TextVariant.BODY)
    }
}

// Para botões, use DSButton com variants:
DSButton(onClick = { }, variant = ButtonVariant.PRIMARY) { Text("Ação Principal") }
DSButton(onClick = { }, variant = ButtonVariant.SECONDARY) { Text("Ação Secundária") }
```

**Variants de Botão Disponíveis:**
- `ButtonVariant.PRIMARY` - Preto com texto branco
- `ButtonVariant.SECONDARY` - Outlined com texto branco
- `ButtonVariant.POSITIVE` - Verde (confirmar, salvar)
- `ButtonVariant.NEGATIVE` - Vermelho (deletar)
- `ButtonVariant.GHOST` - Transparente

## Troubleshooting

### Parser não reconhece arquivo
- Verificar método `supports(fileName: String)`
- Validar formato do nome do arquivo
- Checar conteúdo do arquivo (encoding, formato)

### Valores multiplicados por 100
- **CAUSA COMUM**: Remover ponto quando é separador decimal
- **SOLUÇÃO**: Verificar formato (americano: 77.12 vs brasileiro: 77,12)
- **ITAÚ CSV**: Usa formato americano (ponto = decimal), NÃO remover!

### UI não atualiza
- Confirmar que `StateFlow` está sendo observado (`collectAsState()`)
- Verificar se eventos chegam ao ViewModel (adicionar logs)
- Checar recomposição desnecessária

### Erro de compilação multiplataforma
- Garantir imports de `commonMain`
- Usar `expect/actual` para código específico de plataforma
- Verificar dependências em `build.gradle.kts`

## Recursos Úteis

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Material 3 Design](https://m3.material.io/)

## Boas Práticas

### Código
✅ Use `Result<T>` para operações que podem falhar  
✅ Prefira `suspend fun` para operações assíncronas  
✅ Use `LazyColumn` para listas grandes  
✅ Cache dados processados quando possível  
✅ Parse arquivos em background (coroutines)  

### Git
✅ Commits pequenos e descritivos  
✅ Testar antes de commit  
✅ Documentar mudanças significativas  

---

**Este agente está pronto para evoluir o FinanceHub mantendo qualidade, consistência e aderência ao Design System!** 🚀