# apptudonosso (Kotlin + Compose, super simples)

Este é um esqueleto **mínimo** para abrir no Android Studio ou VS Code.
Sem Room/Retrofit/DataStore/ExoPlayer por enquanto. Tudo mockado e pronto
para evoluir depois.

## Requisitos

- **JDK 17** (Amazon Corretto, Temurin, etc.)
- **Android SDK** (se for usar Android Studio)
- Opcional: **Gradle 8.13** instalado no PATH (para gerar o wrapper)

## Como abrir e rodar

### Opção A — Android Studio (recomendado)
1. `File > Open…` e selecione a pasta do projeto.
2. Garanta que o **Gradle JDK** seja 17 em `Settings > Build > Gradle`.
3. Abra o **Terminal** do Android Studio e rode:
   ```bash
   gradle wrapper --gradle-version 8.13
   ./gradlew assembleDebug
   ```
4. `Run` no emulador ou dispositivo.

### Opção B — VS Code
1. Instale o **JDK 17**.
2. (Opcional) Instale **Gradle 8.13** (SDKMAN no Linux/macOS, winget/choco no Windows).
3. No terminal da pasta do projeto:
   ```bash
   gradle wrapper --gradle-version 8.13
   ./gradlew assembleDebug   # Linux/macOS
   gradlew.bat assembleDebug # Windows
   ```
4. Instale o APK gerado: `app/build/outputs/apk/debug/app-debug.apk` no seu dispositivo (via `adb install`).

> Observação: O **Gradle Wrapper** (arquivo `gradle-wrapper.jar`) não é versionado aqui.
> Você o gera localmente com `gradle wrapper --gradle-version 8.13`.

## Como saber se funcionou
- O comando `assembleDebug` termina **sem erros**.
- Abrindo o app, você vê a **Home** com:
  - Cabeçalho expansível (setas ⬅ ➡ e ícone central ⬆⬇).
  - Botão “TELA INICIAL” (indicativo) e “INSTRUÇÕES” (navega para rota vazia).
  - Área de anúncios (cards com textos mock).
  - Carrossel horizontal “Vídeo mock #X”.
  - Quatro botões empilhados com contadores mock.
  - Rodapé com Cronômetro, Casa, Perfil e FAB “SORTEIO”.

## Estrutura
- `presentation/` (Compose + navegação)
- `domain/` (modelos)
- `data/` (mocks)
