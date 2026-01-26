# FinanceHub KMP - AGENTS.md

Agente principal de desenvolvimento do **FinanceHub**, aplicativo Kotlin Multiplatform de gestão financeira pessoal.

> Este arquivo segue o padrão [agents.md](https://agents.md/) para fornecer contexto a agentes de IA.

## Project Overview

FinanceHub é um app de gestão financeira pessoal que:
- Processa extratos bancários (CSV, TXT, PDF) de múltiplas instituições
- Categoriza transações automaticamente com possibilidade de edição manual
- Gera relatórios consolidados por período
- Funciona em Desktop (macOS/Windows/Linux), Android e iOS

### Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin Multiplatform | 2.0.0 | Shared business logic |
| Compose Multiplatform | 1.6.10 | Declarative UI (Material 3) |
| Kotlinx Coroutines | 1.8.1 | Async operations |
| Kotlinx DateTime | 0.6.0 | Date handling |
| Kotlinx Serialization | 1.6.3 | JSON export |
| PDFBox | 3.0.1 | PDF parsing (Desktop only) |
| Clikt | 4.2.2 | CLI commands |

### Architecture

```
/AGENTS.md                    ← You are here (main agent)
/composeApp/AGENTS.md         ← Design System Dark Mode
/shared/AGENTS.md             ← KMP Architecture + Parsers
/cli/AGENTS.md                ← CLI (fhub) commands

shared/                       # Business logic (Kotlin Multiplatform)
├── domain/                   # Entities, UseCases, Repository interfaces
├── data/                     # Parsers, Repository implementations
└── presentation/             # ViewModels, States, Events (MVI)

composeApp/                   # UI (Compose Multiplatform)
├── designsystem/             # Design System (DSButton, DSCard, DSColors...)
├── theme/                    # MaterialTheme wrapper (FinanceHubTheme)
└── *.kt                      # Screens and components

cli/                          # Command Line Interface
└── src/main/kotlin/          # Clikt commands (fhub)
```

## Setup Commands

```bash
# Install dependencies and build
./gradlew build

# Run desktop app
./gradlew :composeApp:run

# Build CLI fat JAR
./gradlew :cli:shadowJar

# Run CLI directly
java -jar cli/build/libs/fhub-1.0.0.jar --help
```

## Build and Test Commands

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Build DMG for macOS (current architecture)
./gradlew :composeApp:packageDmg

# Build for both architectures (local script)
./local-release.sh

# Build ARM64 only (faster for testing)
./local-release.sh --arm64-only

# Build without uploading to GitHub
./local-release.sh --skip-upload
```

## Code Style Guidelines

### Kotlin Conventions
- Use `Result<T>` for operations that can fail
- Prefer `suspend fun` for async operations
- Use `StateFlow` for observable state in ViewModels
- Follow MVI pattern: Event → ViewModel → State → UI

### File Naming
- Parsers: `{Bank}{Type}Parser.kt` (e.g., `ItauCreditCardParser.kt`)
- ViewModels: `{Feature}ViewModel.kt`
- Screens: `{Feature}Screen.kt` or descriptive name

### Imports
- Always import from `commonMain` for shared code
- Use `expect/actual` for platform-specific implementations

## Testing Instructions

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :shared:test
./gradlew :cli:test

# Run and verify visually
./gradlew :composeApp:run
```

### Manual Testing Checklist
- [ ] Parse sample CSV/TXT files from `Notas/` folder
- [ ] Verify category auto-detection
- [ ] Test category editing
- [ ] Export JSON changelog
- [ ] Generate monthly report

## Security Considerations

- Never hardcode API keys or credentials
- Bank statement files are processed locally only
- No data is sent to external servers
- Keep sensitive test data in `.gitignore`

## PR/Commit Guidelines

### Commit Format
```
type(scope): description

Types: feat, fix, refactor, docs, test, chore
Scopes: parser, ui, cli, build, theme
```

### Examples
```
feat(parser): add Nubank credit card parser
fix(ui): correct dark mode colors on transaction list
refactor(cli): extract export logic to separate command
```

## DMG Packaging

### Local Build
```bash
# ARM64 (Apple Silicon)
./gradlew :composeApp:packageDmg

# Both architectures
./local-release.sh
```

### CI/CD (GitHub Actions)
- Trigger: Push tag `v*` (e.g., `v1.0.5`)
- Matrix: `macos-14` (ARM64), `macos-13` (x64)
- Artifacts: `FinanceHub-{version}-arm64.dmg`, `FinanceHub-{version}-x64.dmg`

---

## Future: Apple Notarization

> **Status**: Planned for future release. Requires Apple Developer Account.

### Requirements
1. **Apple Developer Account** ($99/year)
2. **Developer ID Application certificate**
3. **App-specific password** for notarytool

### Environment Variables (CI/CD)
```yaml
APPLE_DEVELOPER_ID: "Developer ID Application: Your Name (TEAMID)"
APPLE_ID: "your@email.com"
APPLE_APP_SPECIFIC_PASSWORD: "xxxx-xxxx-xxxx-xxxx"
APPLE_TEAM_ID: "XXXXXXXXXX"
```

### Notarization Steps
```bash
# 1. Sign the app
codesign --deep --force --verify --verbose \
  --sign "$APPLE_DEVELOPER_ID" \
  --options runtime \
  --entitlements entitlements.plist \
  "FinanceHub.app"

# 2. Create DMG
hdiutil create -volname "FinanceHub" \
  -srcfolder "FinanceHub.app" \
  -ov -format UDZO "FinanceHub.dmg"

# 3. Submit for notarization
xcrun notarytool submit "FinanceHub.dmg" \
  --apple-id "$APPLE_ID" \
  --password "$APPLE_APP_SPECIFIC_PASSWORD" \
  --team-id "$APPLE_TEAM_ID" \
  --wait

# 4. Staple ticket
xcrun stapler staple "FinanceHub.dmg"
```

### Entitlements File (entitlements.plist)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.security.cs.allow-jit</key>
    <true/>
    <key>com.apple.security.cs.allow-unsigned-executable-memory</key>
    <true/>
    <key>com.apple.security.cs.disable-library-validation</key>
    <true/>
</dict>
</plist>
```

---

## Quick Reference

| Task | Command |
|------|---------|
| Run app | `./gradlew :composeApp:run` |
| Build CLI | `./gradlew :cli:shadowJar` |
| Package DMG | `./gradlew :composeApp:packageDmg` |
| Release (local) | `./local-release.sh` |
| Run tests | `./gradlew test` |
| Clean | `./gradlew clean` |

---

**See specialized agents in subfolders for detailed guidelines on Design System, Architecture, and CLI.**
