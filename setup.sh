#!/bin/bash

# Script de setup inicial do FinanceHub

echo "🚀 FinanceHub - Setup Inicial"
echo "=============================="
echo ""

# Verificar Java
echo "1️⃣ Verificando Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "   ✅ Java encontrado: $JAVA_VERSION"
else
    echo "   ❌ Java não encontrado!"
    echo "   Instale Java 11+ em: https://adoptium.net/"
    exit 1
fi

# Verificar Gradle
echo ""
echo "2️⃣ Verificando Gradle..."
if [ -f "gradlew" ]; then
    echo "   ✅ Gradle wrapper encontrado"
else
    echo "   ❌ gradlew não encontrado!"
    exit 1
fi

# Dar permissão ao gradlew
chmod +x gradlew

# Download de dependências
echo ""
echo "3️⃣ Baixando dependências..."
./gradlew dependencies --console=plain

# Build inicial
echo ""
echo "4️⃣ Build inicial..."
./gradlew build --console=plain -x test

# Verificar estrutura de diretórios
echo ""
echo "5️⃣ Verificando estrutura..."
if [ -d "composeApp" ] && [ -d "shared" ]; then
    echo "   ✅ Estrutura de diretórios OK"
else
    echo "   ❌ Estrutura de diretórios incorreta!"
    exit 1
fi

# Criar diretório de exemplo (se não existir)
EXAMPLE_DIR="$HOME/FinanceHub-Data"
if [ ! -d "$EXAMPLE_DIR" ]; then
    echo ""
    echo "6️⃣ Criando diretório de exemplo..."
    mkdir -p "$EXAMPLE_DIR"
    echo "   ✅ Criado: $EXAMPLE_DIR"
    echo ""
    echo "   📝 Coloque seus arquivos CSV/TXT/PDF neste diretório"
else
    echo ""
    echo "6️⃣ Diretório de exemplo já existe: $EXAMPLE_DIR"
fi

# Instruções finais
echo ""
echo "✅ Setup completo!"
echo ""
echo "📖 Próximos passos:"
echo "   1. Coloque seus extratos em: $EXAMPLE_DIR"
echo "   2. Execute: ./gradlew :composeApp:run"
echo "   3. Na aplicação, selecione o diretório: $EXAMPLE_DIR"
echo ""
echo "📚 Documentação:"
echo "   - README.md - Visão geral"
echo "   - CONFIG.md - Configuração"
echo "   - DEVELOPMENT.md - Notas técnicas"
echo ""
echo "🎉 Bom uso!"
