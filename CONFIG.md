# Configuração do FinanceHub

## Diretório de Dados

Por padrão, a aplicação busca arquivos em:
```
/Users/joaogabriel/Notas
```

Para alterar, edite o arquivo:
- **Desktop**: `composeApp/src/desktopMain/kotlin/Main.kt`
- **Android**: Usuário seleciona via interface

## Formatos Suportados

### 1. Cartão de Crédito Itaú

**Nome do arquivo**: Deve começar com `Itau-` e terminar com `.csv`

**Exemplo**: `Itau-2025-08.csv`

**Formato**:
```csv
data;descricao;valor
01/08/2025;IFOOD DELIVERY;85.50
02/08/2025;UBER VIAGEM;32.00
05/08/2025;ESTORNO UBER;-32.00
```

**Notas**:
- Valores positivos = gastos (convertidos para negativo internamente)
- Valores negativos = estornos/créditos

### 2. Extrato Bancário Itaú

**Nome do arquivo**: Deve começar com `Itau-extrato` e terminar com `.txt` ou `.csv`

**Exemplo**: `Itau-extrato-2025-08.txt`

**Formato**:
```txt
data;descricao;valor
01/08/2025;SALARIO MENSAL;5000.00
05/08/2025;PAGAMENTO BOLETO;-150.00
10/08/2025;PIX RECEBIDO;200.00
```

**Notas**:
- Valores positivos = entradas (receitas)
- Valores negativos = saídas (despesas)

### 3. Extrato PicPay

**Nome do arquivo**: Deve conter `picpay` (case-insensitive) e terminar com `.pdf`

**Exemplo**: `Extrato-PicPay-Agosto-2025.pdf`

**Formato**: PDF padrão do PicPay

**Notas**:
- Parsing automático de texto do PDF
- Identifica entradas/saídas por keywords
- Keywords de entrada: "recebido", "entrada", "crédito"
- Keywords de saída: "pago", "saida", "débito"

## Categorias Automáticas

O sistema categoriza automaticamente usando keywords:

| Categoria | Keywords |
|-----------|----------|
| **Alimentação** | ifood, rappi, uber eats, restaurante, mercado, supermercado |
| **Transporte** | uber, 99, taxi, combustivel, gasolina, estacionamento |
| **Saúde** | farmacia, drogaria, hospital, clinica, medico, dentista |
| **Lazer** | cinema, streaming, netflix, spotify, youtube, disney |
| **Educação** | curso, faculdade, escola, livro, udemy, alura |
| **Compras** | mercado livre, amazon, shopee, magalu, americanas |
| **Contas** | energia, agua, internet, telefone, aluguel, condominio |
| **Investimentos** | investimento, aplicacao, cdb, tesouro, acao, fundo |
| **Transferências** | transferencia, pix, ted, doc, p2p |
| **Salário** | salario, pagamento, vencimento, remuneracao |
| **Outros** | Tudo que não se encaixa acima |

### Customização de Categorias

Para adicionar/modificar categorias, edite:
```
shared/src/commonMain/kotlin/com/financehub/domain/model/Category.kt
```

Exemplo:
```kotlin
ALIMENTACAO(
    "Alimentação",
    listOf(
        "ifood", "rappi", "uber eats",
        // Adicione suas keywords aqui
        "minha_keyword"
    )
)
```

## Troubleshooting

### Problema: Nenhuma transação encontrada

**Soluções**:
1. Verifique se os arquivos estão no diretório correto
2. Confirme que os nomes dos arquivos seguem o padrão
3. Verifique o formato do conteúdo dos arquivos
4. Certifique-se de que o encoding é UTF-8

### Problema: Valores incorretos

**Soluções**:
1. Verifique se o separador decimal está correto (vírgula)
2. Confirme que não há espaços extras nos valores
3. Valide se o formato de data está correto (dd/MM/yyyy)

### Problema: PDF não processado

**Soluções**:
1. Apenas funciona em Desktop (não mobile ainda)
2. Verifique se o nome contém "picpay"
3. Confirme que é um PDF válido e não corrompido
4. Teste com um PDF mais simples primeiro

## Performance

### Recomendações:
- **Transações**: Até 10.000 por mês (testado)
- **Arquivos**: Até 50 arquivos por diretório
- **PDFs**: Até 10MB cada

### Otimização:
Se tiver muitos arquivos, considere:
1. Separar por ano em diretórios diferentes
2. Processar em lotes menores
3. Arquivar meses antigos

## Backup

**Importante**: A aplicação não modifica seus arquivos originais!

Recomendamos:
1. Manter backups dos arquivos originais
2. Não deletar extratos após processar
3. Versionar relatórios gerados

## Próximas Features

Planejado para próximas versões:
- ✨ Upload de arquivos via drag & drop
- ✨ Suporte a outros bancos
- ✨ Export para Excel/PDF
- ✨ Sincronização cloud
- ✨ App mobile iOS/Android
