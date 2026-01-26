#!/bin/bash
set -e

echo "🚀 Local Release Script"
echo "======================"
echo ""

# Parse arguments
ARM64_ONLY=false
SKIP_UPLOAD=false
for arg in "$@"; do
    case $arg in
        --arm64-only)
            ARM64_ONLY=true
            echo "📌 Mode: ARM64 only (skipping x64 build)"
            ;;
        --skip-upload)
            SKIP_UPLOAD=true
            echo "📌 Mode: Skip GitHub upload"
            ;;
        --help|-h)
            echo "Usage: ./local-release.sh [options]"
            echo ""
            echo "Options:"
            echo "  --arm64-only    Build only ARM64 DMG (faster for testing)"
            echo "  --skip-upload   Skip uploading to GitHub Release"
            echo "  --help, -h      Show this help message"
            exit 0
            ;;
    esac
done
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Calculate next version
echo -e "${BLUE}📋 Step 1: Calculate next version${NC}"
git fetch --tags
LATEST_TAG=$(git tag -l 'v*' | sort -V | tail -n1)

if [ -z "$LATEST_TAG" ]; then
    NEW_VERSION="1.0.0"
    echo "No existing tags, starting at v1.0.0"
else
    echo "Latest tag: $LATEST_TAG"
    VERSION="${LATEST_TAG#v}"
    IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"
    PATCH=$((PATCH + 1))
    NEW_VERSION="$MAJOR.$MINOR.$PATCH"
fi

NEW_TAG="v$NEW_VERSION"
echo -e "${GREEN}✅ New version: $NEW_TAG${NC}"
echo ""

# Check for commits since last tag
if [ -n "$LATEST_TAG" ]; then
    COMMITS=$(git log --oneline "$LATEST_TAG"..HEAD --no-decorate)
    if [ -z "$COMMITS" ]; then
        echo -e "${YELLOW}⚠️  No new commits since $LATEST_TAG.${NC}"
        echo -e "${YELLOW}Using existing tag $LATEST_TAG for DMG upload.${NC}"
        NEW_TAG="$LATEST_TAG"
        NEW_VERSION="${LATEST_TAG#v}"
        COMMITS="No new commits - rebuilding DMGs for existing release"
    else
        echo "Commits since $LATEST_TAG:"
        echo "$COMMITS"
        echo ""
    fi
fi

# Step 2: Create and push tag (only if new)
if [ "$NEW_TAG" != "$LATEST_TAG" ]; then
    echo -e "${BLUE}🏷️  Step 2: Create and push tag${NC}"
    git tag -a "$NEW_TAG" -m "Release $NEW_TAG"
    git push origin "$NEW_TAG"
    echo -e "${GREEN}✅ Tag created and pushed${NC}"
    echo ""
else
    echo -e "${BLUE}🏷️  Step 2: Using existing tag${NC}"
    echo "Skipping tag creation, will upload DMGs to existing release $NEW_TAG"
    echo ""
fi

# Step 3: Setup output directory
echo -e "${BLUE}📁 Step 3: Setup output directory${NC}"
OUTPUT_DIR="$PWD/dmg-output"
if [ -d "$OUTPUT_DIR" ]; then
    echo "Removing existing DMGs from $OUTPUT_DIR..."
    rm -rf "$OUTPUT_DIR"
fi
mkdir -p "$OUTPUT_DIR"
echo -e "${GREEN}✅ Output directory ready${NC}"
echo ""

# Step 4: Build ARM64 DMG
echo -e "${BLUE}🔨 Step 4: Build ARM64 DMG${NC}"
./gradlew clean
echo "Building CLI JAR first..."
./gradlew :cli:shadowJar --no-daemon
./gradlew :composeApp:packageDmg --no-daemon

# Inject Java bin into the app bundle (JPackage doesn't include java CLI)
echo "Injecting Java bin into app bundle..."
APP_PATH="composeApp/build/compose/binaries/main/app/FinanceHub.app"
RUNTIME_BIN="$APP_PATH/Contents/runtime/Contents/Home/bin"
mkdir -p "$RUNTIME_BIN"
# For ARM64, detect the current JDK
CURRENT_JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home 2>/dev/null || echo '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home')}"
echo "Using JAVA_HOME: $CURRENT_JAVA_HOME"
cp "$CURRENT_JAVA_HOME/bin/java" "$RUNTIME_BIN/"
chmod +x "$RUNTIME_BIN/java"
echo "Java binary injected: $RUNTIME_BIN/java"

# Recreate DMG with the updated app
echo "Recreating DMG with Java binary..."
rm -rf composeApp/build/compose/binaries/main/dmg/*.dmg
hdiutil create -volname "FinanceHub" -srcfolder "$APP_PATH" -ov -format UDZO "composeApp/build/compose/binaries/main/dmg/FinanceHub-1.0.0.dmg"

cd composeApp/build/compose/binaries/main/dmg
for f in *.dmg; do
    if [[ ! "$f" == *-arm64.dmg ]]; then
        mv "$f" "${f%.dmg}-arm64.dmg"
    fi
done
cp *.dmg "$OUTPUT_DIR/"
cd - > /dev/null
echo -e "${GREEN}✅ ARM64 DMG built${NC}"
echo ""

# Skip x64 build if --arm64-only flag is set
if [ "$ARM64_ONLY" = true ]; then
    echo -e "${YELLOW}⏭️  Skipping x64 build (--arm64-only mode)${NC}"
    echo ""
else
    # Step 5: Download JDK 17 x64
    echo -e "${BLUE}📥 Step 5: Download JDK 17 x64${NC}"
JDK_DIR="$HOME/jdks-temp"
JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_x64_mac_hotspot_17.0.13_11.tar.gz"

mkdir -p "$JDK_DIR"
curl -L "$JDK_URL" -o /tmp/jdk17-x64.tar.gz
tar -xzf /tmp/jdk17-x64.tar.gz -C "$JDK_DIR"
JDK_HOME="$JDK_DIR/jdk-17.0.13+11/Contents/Home"
echo -e "${GREEN}✅ JDK x64 downloaded${NC}"
echo ""

# Step 6: Build x64 DMG
echo -e "${BLUE}🔨 Step 6: Build x64 DMG${NC}"
export JAVA_HOME="$JDK_HOME"
export PATH="$JDK_HOME/bin:$PATH"

# Increase memory for x64 cross-compilation (Rosetta 2 needs more memory)
export GRADLE_OPTS="-Xmx10g -Xms4g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC"

./gradlew clean
echo "Building CLI JAR first..."
arch -x86_64 ./gradlew :cli:shadowJar --no-daemon --max-workers=2
arch -x86_64 ./gradlew :composeApp:packageDmg --no-daemon --max-workers=2

# Inject Java bin into the app bundle (JPackage doesn't include java CLI)
echo "Injecting Java bin into app bundle..."
APP_PATH="composeApp/build/compose/binaries/main/app/FinanceHub.app"
RUNTIME_BIN="$APP_PATH/Contents/runtime/Contents/Home/bin"
mkdir -p "$RUNTIME_BIN"
# Copy java executable from the x64 JDK
cp "$JDK_HOME/bin/java" "$RUNTIME_BIN/"
chmod +x "$RUNTIME_BIN/java"
echo "Java binary injected: $RUNTIME_BIN/java"

# Recreate DMG with the updated app
echo "Recreating DMG with Java binary..."
rm -rf composeApp/build/compose/binaries/main/dmg/*.dmg
hdiutil create -volname "FinanceHub" -srcfolder "$APP_PATH" -ov -format UDZO "composeApp/build/compose/binaries/main/dmg/FinanceHub-1.0.0.dmg"

cd composeApp/build/compose/binaries/main/dmg
for f in *.dmg; do
    if [[ ! "$f" == *-x64.dmg ]]; then
        mv "$f" "${f%.dmg}-x64.dmg"
    fi
done
cp *.dmg "$OUTPUT_DIR/"
cd - > /dev/null
echo -e "${GREEN}✅ x64 DMG built${NC}"
echo ""

    # Step 7: Clean up JDK
    echo -e "${BLUE}🧹 Step 7: Clean up temporary JDK${NC}"
    rm -rf "$JDK_DIR"
    rm -f /tmp/jdk17-x64.tar.gz
    echo -e "${GREEN}✅ JDK cleaned up${NC}"
    echo ""
fi  # End of x64 build block

# Step 8: List generated DMGs
echo -e "${BLUE}📦 Step 8: Generated DMGs${NC}"
ls -lh "$OUTPUT_DIR"
echo ""

# Skip upload if --skip-upload flag is set
if [ "$SKIP_UPLOAD" = true ]; then
    echo -e "${YELLOW}⏭️  Skipping GitHub upload (--skip-upload mode)${NC}"
    echo ""
else
    # Step 9: Upload to GitHub Release
    echo -e "${BLUE}⬆️  Step 9: Upload to GitHub Release${NC}"
    echo "Uploading DMGs to release $NEW_TAG..."
    gh release create "$NEW_TAG" "$OUTPUT_DIR"/*.dmg \
        --title "FinanceHub $NEW_TAG" \
        --notes "## Downloads

- **Video-Clipper-*-arm64.dmg** - Para Macs com Apple Silicon (M1, M2, M3, M4)
- **Video-Clipper-*-x64.dmg** - Para Macs Intel

### Instalação
1. Baixe o DMG correspondente ao seu Mac
2. Abra o DMG e arraste o app para Applications
3. Na primeira execução, clique com botão direito > Abrir (para bypass do Gatekeeper)

### Changes
$COMMITS" || {
    echo -e "${YELLOW}⚠️  Release já existe, fazendo upload dos assets...${NC}"
    gh release upload "$NEW_TAG" "$OUTPUT_DIR"/*.dmg --clobber
}

    echo -e "${GREEN}✅ Release uploaded${NC}"
    echo ""
fi  # End of upload block

# Summary
echo "======================================"
echo -e "${GREEN}🎉 Release Complete!${NC}"
echo "======================================"
echo "Version: $NEW_TAG"
if [ "$SKIP_UPLOAD" = false ]; then
    echo "Release URL: https://github.com/$(git remote get-url origin | sed 's/.*github.com[:/]\(.*\)\.git/\1/')/releases/tag/$NEW_TAG"
fi
echo ""
echo "DMGs available at: $OUTPUT_DIR"
ls "$OUTPUT_DIR"
echo ""
