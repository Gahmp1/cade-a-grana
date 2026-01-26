# Cadê a Grana? 💰

Sistema de análise financeira construído com **Kotlin Multiplatform** e **Compose Multiplatform** para processar extratos bancários e gerar relatórios detalhados.

## 🎯 Características

- ✅ **Multiplataforma**: macOS, iOS, Android
- ✅ **Processamento Multi-Fonte**:
  - Cartão de Crédito Itaú (CSV)
  - Extrato Bancário Itaú (TXT)
  - Extrato PicPay (PDF)
- ✅ **Categorização Automática**: 11 categorias inteligentes
- ✅ **Relatórios Detalhados**: Mensais e consolidados
- ✅ **UI Moderna**: Material Design 3 com Compose

## 🏗️ Arquitetura

```
CadeAGrana/
├── composeApp/          # UI Layer (Compose Multiplatform)
│   ├── commonMain/      # UI compartilhada
│   ├── desktopMain/     # Main desktop
│   └── androidMain/     # Main Android
│
└── shared/              # Business Logic
    ├── commonMain/
    │   ├── domain/      # Models, UseCases, Repositories
    │   └── data/        # Parsers, Repository impl
    └── desktopMain/     # Implementações específicas Desktop
```

### Camadas

1. **Domain Layer**: Models puros, interfaces de repository, use cases
2. **Data Layer**: Implementação de parsers e repositórios
3. **Presentation Layer**: ViewModels com StateFlow
4. **UI Layer**: Compose Multiplatform

## 🚀 Como Executar

### Pré-requisitos

- Java 11+
- Kotlin 2.0+
- Gradle 8.2+
- Para iOS: Xcode + CocoaPods

### Desktop (macOS)

```bash
./gradlew :composeApp:run
```

### Android

```bash
./gradlew :composeApp:installDebug
```

### iOS

```bash
cd iosApp
pod install
open iosApp.xcworkspace
```

## 📊 Formato dos Arquivos

### Cartão Itaú (CSV)
```csv
data;descricao;valor
01/08/2025;IFOOD;50.00
02/08/2025;UBER;25.50
```

### Extrato Bancário Itaú (TXT)
```txt
data;descricao;valor
01/08/2025;SALARIO;5000.00
05/08/2025;PAGAMENTO BOLETO;-150.00
```

### PicPay (PDF)
- Formato PDF padrão do PicPay
- Extração automática via PDFBox

## 🗂️ Categorias

1. **Alimentação**: Restaurantes, iFood, mercados
2. **Transporte**: Uber, 99, combustível
3. **Saúde**: Farmácias, consultas
4. **Lazer**: Streaming, cinema
5. **Educação**: Cursos, livros
6. **Compras**: E-commerce
7. **Contas**: Energia, água, internet
8. **Investimentos**: Aplicações financeiras
9. **Transferências**: PIX, TED
10. **Salário**: Receitas salariais
11. **Outros**: Demais transações

## 🛠️ Tecnologias

- **Kotlin Multiplatform 2.0**
- **Compose Multiplatform 1.6.10**
- **Kotlinx Coroutines 1.8.1**
- **Kotlinx DateTime 0.6.0**
- **Kotlinx Serialization 1.6.3**
- **Apache PDFBox** (Desktop PDF parsing)

## 📱 Roadmap

### Fase 1: Base ✅ (Completo)
- [x] Estrutura do projeto KMP
- [x] Models de domínio
- [x] Parsers (CSV, TXT)
- [x] Repository e Use Cases
- [x] ViewModel básico

### Fase 2: UI Desktop ✅ (Completo)
- [x] Tela principal
- [x] Seleção de diretório
- [x] Visualização de relatórios
- [x] Cards de resumo

### Fase 3: Parser PDF 🔄 (Em progresso)
- [x] Estrutura base
- [ ] Integração PDFBox Desktop
- [ ] Testes com extratos reais

### Fase 4: UI Avançada 📋 (Planejado)
- [ ] Gráficos de despesas
- [ ] Filtros por categoria
- [ ] Exportação de relatórios
- [ ] Tema escuro

### Fase 5: Mobile 📱 (Planejado)
- [ ] Implementação iOS
- [ ] SAF Android (Storage Access Framework)
- [ ] Câmera para escanear documentos
- [ ] Sincronização cloud

### Fase 6: Features Avançadas 🚀 (Futuro)
- [ ] Machine Learning para categorização
- [ ] Previsões financeiras
- [ ] Metas e alertas
- [ ] Multi-usuário

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👤 Autor

**João Gabriel**

---

⭐ Se este projeto te ajudou, considere dar uma estrela!
