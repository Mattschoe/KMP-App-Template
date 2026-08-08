# {{DISPLAY_NAME}}

<!-- TODO: one or two sentences on what this app actually does. -->

Package: `{{PACKAGE}}` · Kotlin Multiplatform (Android · desktop · iOS) with a single shared module.

## Tech Stack

| Area | Choice |
|---|---|
| UI | Compose Multiplatform 1.11.1, Material 3 |
| Targets | Android (minSdk 24 / targetSdk 37), JVM desktop, iOS (arm64 + simulator) |
| Async + state | Coroutines, `StateFlow`, `collectAsStateWithLifecycle()` |
| Dependency injection | Manual — `AppContainer`, no Hilt/Dagger/Koin |
| Navigation | Navigation Compose with type-safe `@Serializable` routes |
| Persistence | Room 2.8.4 (KSP, bundled SQLite) + DataStore Preferences |
| Build | AGP 9.2.1, Gradle 9.5.1, Kotlin 2.4.10, version catalog |

## Build, Test & Run

```bash
./gradlew :androidApp:assembleDebug     # debug APK
./gradlew :androidApp:installDebug      # install on the connected device
./gradlew :desktopApp:run               # desktop app
./gradlew :shared:jvmTest               # unit tests
./gradlew build                         # everything
```

**iOS builds only on macOS** — Kotlin/Native cannot link Apple targets on other hosts, so `shared/build.gradle.kts` declares them conditionally. Everything above stays green on Linux and Windows; iOS is covered by the macOS CI job.

## Architecture

One shared module holds the whole app; the three app modules are thin platform shells that build an `AppContainer` and hand it to `App()`.

- `domain/` — models, repository interfaces, `Result`/`Error`. No platform types.
- `data/` — Room + DataStore in `data/local/`, implementations in `data/repositories/`.
- `ui/` — `pages/<feature>/` (one dir per screen), `navigation/`, `components/`, `theme/`.

Dependency injection is manual via `AppContainer`; ViewModels are constructed in `ApplicationNavigationHost.kt`. See `CLAUDE.md` for the full file map and the recipes for adding new things.

## Project Layout

```
shared/                     the whole app: UI, domain, data
  src/commonMain/kotlin/    AppContainer, App, domain/, data/, ui/
  src/androidMain/kotlin/   MyApplication + Android Room/DataStore builders
  src/jvmMain/kotlin/       createAppContainer() + desktop builders
  src/iosMain/kotlin/       MainViewController + iOS builders
  src/jvmTest/kotlin/       JVM unit tests
  src/commonMain/composeResources/
                            values/strings.xml (source of truth) + values-XX/
androidApp/                 MainActivity, manifest, icons, signing config
desktopApp/                 main() window wrapper
iosApp/                     Xcode project
```

### Where to add new things

1. **A screen** — route in `PageNavigation.kt` → `composable<>` block in `ApplicationNavigationHost.kt` → `ui/pages/<name>/XPage.kt` + `XViewModel.kt` → strings in every locale file.
2. **Persisted data** — entity in `Entities.kt` → DAO → register on `AppDatabase` → bump the version *and write the migration* → repository interface + implementation → expose on `AppContainer`.
3. **A platform service** — interface in `domain/services/`, one implementation per platform, passed into `AppContainer` from all three composition roots.

## Testing Notes

`StringResourceCompletenessTest` (in `shared/src/jvmTest/`) parses every `values-XX/strings.xml` off disk and fails on missing keys, blank values, mismatched `%n$s` placeholders, or keys that exist in a translation but not in the default. Adding a string means adding it to **every** locale file.

## CI/CD

- **`release-please.yml`** — on push to `main`, maintains a release PR built from your Conventional Commits (`feat:`, `fix:`, ...). Merging it tags the release and updates `CHANGELOG.md`.
- **`release-artifact-upload.yml`** — on release publish, attaches a signed Android APK and an iOS archive.

Required repository secrets:

| Secret | Used for |
|---|---|
| `RELEASE_PLEASE_TOKEN` | a PAT so release-please's PR triggers other workflows |
| `KEYSTORE_BASE64` | base64 of the release keystore (`base64 -w0 keystore.jks`) |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | signing key alias |
| `KEY_PASSWORD` | signing key password |

Without the keystore secrets the Android job still runs and produces an unsigned APK. The iOS job builds unsigned by default; see `docs/` for the signed `.ipa` upgrade path.
