# KMP App Template

My opinionated template for building Kotlin Multiplatform apps. 

**It uses:** 
- Shared UI across Android, desktop and IOS
- Manual DI
- Room + DataStore
- Type-safe navigation
- GitHub release automation ([release-please](https://github.com/googleapis/release-please))
- App builds attaching to GitHub releases ([action-release-apk](https://github.com/Mattschoe/action-release-apk))

This repository is a template repository. Create a repo from it, run `./bootstrap.sh`, and you have a project that builds on all three platforms and already follows the conventions described in `AGENTS.md`. Be aware that `AGENTS.md` is also opinionated and you should modify this to however you like.

## Getting started

```bash
gh repo create my-app --private --template Mattschoe/KMP-App-Template --clone
cd my-app
./bootstrap.sh --package com.mycompany.myapp --name "My App"
```

`bootstrap.sh` rewrites the Kotlin package, the Gradle project name, the Android `applicationId`, the iOS bundle identifier and the Compose-resources package, 
moves the source directories to match, then deletes itself and starts a fresh git history.

| Flag | Effect | Required? |
|---|---|---|
| `--package com.foo.bar` | The Kotlin package | Y |
| `--name "My App"` | Display name, also becomes `rootProject.name` (stripped to `MyApp`) | Y |
| `--app-id com.foo.bar` | Android `applicationId`, if it differs from the package | N | 
| `--locales da,de,fr` | creates `values-XX/strings.xml` seeded from the default file | N |
| `--no-ios` / `--no-desktop` | drops that platform entirely | N |
| `--keep-git` | keeps the template's git history | N |
| `--dry-run` | prints what would change and exits | N |

## Tech Stack

| Area | Choice |
|---|---|
| UI | Compose Multiplatform 1.11.1, Material 3 |
| Targets | Android (minSdk 24 / targetSdk 37), JVM desktop, iOS (arm64 + simulator) |
| Async + state | Coroutines, `StateFlow`, `collectAsStateWithLifecycle()` |
| Dependency injection | Manual. I use a `AppContainer` class, no Hilt/Dagger/Koin |
| Navigation | Navigation Compose with type-safe `@Serializable` routes |
| Persistence | Room 2.8.4 (KSP, bundled SQLite) + DataStore Preferences |
| Build | AGP 9.2.1, Gradle 9.5.1, Kotlin 2.4.10 |
| Release | release-please (Conventional Commits) + signed APK and iOS archive on publish |

## Project Layout

The template uses the AGP 9.0 standard.

### Where to add new things
#### A screen
Route in `PageNavigation.kt` → `composable<>` block in `ApplicationNavigationHost.kt` → `ui/pages/<name>/XPage.kt` + `XViewModel.kt` → strings in every locale file.

#### Persisted data
Entity in `Entities.kt` → DAO → register on `AppDatabase` → bump the version *and write the migration* → repository interface + implementation → expose on `AppContainer`.

#### A platform service
Interface in `domain/services/`, one implementation per platform, passed into `AppContainer` from all three composition roots.

## CI/CD

Two workflows:

- `release-please.yml`: Automates releaselogs via Conventional Commits. See [release-please](https://github.com/googleapis/release-please)
- `release-artifact-upload.yml`: Attaches a signed Android APK, and an iOS archive on Github Releases. See [action-release-apk](https://github.com/Mattschoe/action-release-apk)

Required repository secrets:

| Secret | Used for |
|---|---|
| `RELEASE_PLEASE_TOKEN` | a PAT so release-please's PR triggers other workflows. Needs R/W permission to: Contents, Workflows, Pull requests and Issues |
| `KEYSTORE_BASE64` | base64 of your release keystore (`base64 -w0 keystore.jks`) |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | signing key alias |
| `KEY_PASSWORD` | signing key password |

Without the keystore secrets the Android job still runs and produces an **unsigned** APK; local release builds behave the same way.

**The iOS job builds unsigned by default** (`CODE_SIGNING_ALLOWED=NO`) so it needs no Apple credentials and still catches iOS breakage on every release. To ship an installable `.ipa`, add an Apple certificate (`.p12`), a provisioning profile and an `ExportOptions.plist`, import them with `security import` in the job, drop the `CODE_SIGNING_ALLOWED=NO` flags, and add an `xcodebuild -exportArchive` step.

# Examples
Examples of this template can be seen in:
- [Shopping Made Easy](https://github.com/Mattschoe/Shopping-Made-Easy)
- [Vault](https://github.com/Mattschoe/Vault)
- [Steward](https://github.com/Mattschoe/steward)
