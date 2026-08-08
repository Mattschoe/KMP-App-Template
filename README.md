# App Template

Kotlin Multiplatform app template — Compose Multiplatform UI shared across Android, desktop and iOS, with manual DI, Room + DataStore, type-safe navigation and release automation already wired up.

This repository is a GitHub **template repository**. Create a repo from it, run `./bootstrap.sh`, and you have a project that builds on all three platforms and already follows the conventions described in `CLAUDE.md`.

## Getting started

```bash
gh repo create my-app --private --template Mattschoe/kmp-app-template --clone
cd my-app
./bootstrap.sh --package com.mattschoe.myapp --name "My App"
```

`bootstrap.sh` rewrites the Kotlin package, the Gradle project name, the Android `applicationId`, the iOS bundle identifier and the Compose-resources package, moves the source directories to match, then deletes itself and starts a fresh git history.

| Flag | Effect |
|---|---|
| `--package com.foo.bar` | **required** — the Kotlin package |
| `--name "My App"` | **required** — display name; also becomes `rootProject.name` (stripped to `MyApp`) |
| `--app-id com.foo.bar` | Android `applicationId`, if it differs from the package |
| `--locales da,de,fr` | creates `values-XX/strings.xml` seeded from the default file |
| `--no-ios` / `--no-desktop` | drops that platform entirely |
| `--keep-git` | keeps the template's git history |
| `--dry-run` | prints what would change and exits |

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
| Release | release-please (Conventional Commits) + signed APK and iOS archive on publish |

## Build, Test & Run

```bash
./gradlew :androidApp:assembleDebug     # debug APK
./gradlew :androidApp:installDebug      # install on the connected device
./gradlew :desktopApp:run               # desktop app
./gradlew :shared:jvmTest               # unit tests
./gradlew build                         # everything
```

**iOS builds only on macOS** — Kotlin/Native cannot link Apple targets on other hosts, so `shared/build.gradle.kts` declares them conditionally. Everything above stays green on Linux and Windows; iOS is covered by the macOS CI job.

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

`CLAUDE.md` has the same recipes in more detail, plus the file map.

## CI/CD

Two workflows:

- **`release-please.yml`** — on every push to `main`, maintains a release PR from your Conventional Commits. Merging it tags the release and updates `CHANGELOG.md`.
- **`release-artifact-upload.yml`** — on release publish, builds and attaches a signed Android APK (`ubuntu`) and an iOS archive (`macos`).

Required repository secrets:

| Secret | Used for |
|---|---|
| `RELEASE_PLEASE_TOKEN` | a PAT so release-please's PR triggers other workflows |
| `KEYSTORE_BASE64` | base64 of your release keystore (`base64 -w0 keystore.jks`) |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | signing key alias |
| `KEY_PASSWORD` | signing key password |

Without the keystore secrets the Android job still runs and produces an **unsigned** APK; local release builds behave the same way.

**The iOS job builds unsigned by default** (`CODE_SIGNING_ALLOWED=NO`) so it needs no Apple credentials and still catches iOS breakage on every release. To ship an installable `.ipa`, add an Apple certificate (`.p12`), a provisioning profile and an `ExportOptions.plist`, import them with `security import` in the job, drop the `CODE_SIGNING_ALLOWED=NO` flags, and add an `xcodebuild -exportArchive` step.

## Optional extras

`docs/optional-ci/` holds two workflows that are **not** enabled by default — copy them into `.github/workflows/` to turn them on:

- `claude.yml` — responds to `@claude` mentions in issues and PRs.
- `claude-code-review.yml` — automatic review on every PR.
