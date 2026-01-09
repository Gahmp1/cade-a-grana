# Notas de Desenvolvimento

## Decisões de Arquitetura

### 1. Clean Architecture + MVI

Optamos por Clean Architecture com MVI (Model-View-Intent) por:
- **Separação clara de responsabilidades**
- **Testabilidade**: Cada camada pode ser testada isoladamente
- **Manutenibilidade**: Mudanças em uma camada não afetam outras
- **Escalabilidade**: Fácil adicionar novas features

### 2. Kotlin Multiplatform

Escolhemos KMP ao invés de Electron/Streamlit por:
- **Performance nativa**: Melhor que web-based
- **Experiência de usuário**: UI nativa em cada plataforma
- **Conhecimento existente**: Time já familiar com Kotlin/native
- **Futuro mobile**: Facilita expansão para iOS/Android

### 3. Compose Multiplatform

UI com Compose porque:
- **Declarativa**: Mais simples que SwiftUI/XML
- **Compartilhamento**: 90%+ da UI é compartilhada
- **Hot Reload**: Desenvolvimento mais rápido
- **Material 3**: Design system completo

## Estrutura de Dados

### Transaction
Modelo central que representa uma transação financeira:
- `amount`: Positivo = entrada, Negativo = saída
- `source`: Origem do dado (Itaú, PicPay, etc)
- `category`: Categoria automática baseada em keywords

### Normalização de Valores

**Cartão de Crédito (CSV)**:
- Arquivo: valores positivos = gasto
- Sistema: convertemos para negativo
- Razão: Manter consistência (negativo = saída)

**Extrato Bancário (TXT)**:
- Arquivo: positivo = entrada, negativo = saída
- Sistema: mantemos como está
- Razão: Já está normalizado

**PicPay (PDF)**:
- Arquivo: valor + tipo (pago/recebido)
- Sistema: convertemos baseado no tipo
- Razão: PDF não tem sinal negativo

## Parsers

### Design Pattern: Strategy

Cada parser implementa `FileParser`:
```kotlin
interface FileParser {
    fun supports(fileName: String): Boolean
    suspend fun parse(fileContent: String): List<Transaction>
}
```

Vantagens:
- Fácil adicionar novos parsers
- Seleção automática por nome de arquivo
- Isolamento de lógica de parsing

### PDF Parsing (Desktop only)

PDFBox é usado apenas no Desktop porque:
- **JVM only**: Não funciona em iOS/Android nativamente
- **Alternativa mobile**: Usar bibliotecas específicas da plataforma
  - iOS: PDFKit
  - Android: PdfRenderer

## State Management

### StateFlow vs LiveData

Usamos StateFlow porque:
- **Multiplatform**: LiveData é Android-only
- **Cold Flow**: Inicia apenas quando coletado
- **Backpressure**: Melhor controle de fluxo
- **Type-safe**: Kotlin-first

### Eventos vs Estado

**Estado (State)**:
- O que exibir na tela
- `isLoading`, `transactions`, `error`

**Eventos (Event)**:
- Ações do usuário
- `LoadDirectory`, `SelectMonth`

## Dependency Injection

Manual DI ao invés de Koin/Hilt porque:
- **Simplicidade**: Projeto pequeno
- **Zero overhead**: Sem reflection
- **Type-safe**: Compile-time checking
- **Multiplatform**: Funciona em todas as plataformas

Quando crescer, considerar:
- **Koin**: Multiplatform, simples
- **Kodein**: Mais type-safe

## Performance

### Lazy Loading

`MonthReport.categorySummary` é lazy porque:
- Evita cálculos desnecessários
- Só calcula quando acessado
- Cacheado após primeira computação

### Coroutines

Usamos `suspend` em todos os parsers para:
- Não bloquear UI thread
- Fácil cancelamento
- Composabilidade assíncrona

## Testes (TODO)

### Estrutura planejada:

```
shared/
  src/
    commonTest/     # Testes compartilhados
    desktopTest/    # Testes específicos desktop
```

### Estratégia de testes:

1. **Unit Tests** (commonTest):
   - Models: Category.classify()
   - UseCases: LoadTransactionsUseCase
   - Repository: Mock FileReader

2. **Integration Tests** (desktopTest):
   - Parsers com arquivos reais
   - End-to-end workflow

3. **UI Tests** (composeApp):
   - Compose testing library
   - Screenshot tests

## Problemas Conhecidos

### 1. PDF Parsing Desktop-only

**Problema**: PDFBox não funciona em iOS/Android
**Solução futura**: 
- iOS: Implementar com PDFKit
- Android: Usar PdfRenderer ou Apache PDFBox Android

### 2. File System Access Mobile

**Problema**: Acesso direto ao filesystem limitado
**Solução**:
- iOS: Usar Document Picker
- Android: SAF (Storage Access Framework)

### 3. Category Classification

**Problema**: Keywords podem não cobrir todos os casos
**Solução futura**:
- Machine Learning model
- User feedback loop
- Manual re-categorization

## Próximos Passos

### Curto Prazo (1-2 semanas)
1. ✅ Completar parsers básicos
2. ⏳ Testar com dados reais
3. ⏳ Adicionar PDFBox ao Desktop
4. ⏳ Melhorar UI com gráficos

### Médio Prazo (1 mês)
1. Implementar testes unitários
2. Adicionar export de relatórios
3. Suporte a múltiplos bancos
4. Tema escuro

### Longo Prazo (3+ meses)
1. Port para iOS
2. Port para Android
3. Sincronização cloud
4. ML para categorização

## Recursos Úteis

- [KMP Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [PDFBox](https://pdfbox.apache.org/)
- [Kotlinx DateTime](https://github.com/Kotlin/kotlinx-datetime)
