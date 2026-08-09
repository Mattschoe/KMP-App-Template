This file provides guidance when working with code in this repository.

## Planning & Exploration

When planning a task, use the file map and architecture section in this CLAUDE.md to identify the specific files relevant to the task, 
then read those files directly. The file map is a directory — use it to go straight to the 2-3 files that matter instead of exploring 8-10 defensively.

This file describes the *shape* of the codebase — where things live and how they fit together — not an index of every file. 
Rule of thumb: if a file name tells you what it does, just `ls` and open it. Files whose names *don't* tell you that are annotated below.

- **Language**: User-facing strings live in `shared/src/commonMain/composeResources/values/strings.xml` and are referenced as `Res.string.*` — never hardcode display text in a composable.
- **Translations**: only ever edit `values/strings.xml`. Locale files are translations of it; adding a key there without adding it to every locale breaks `StringResourceCompletenessTest`.
- **Commits**: Conventional Commits (`feat:`, `fix:`, `chore:`, with an optional scope like `fix(home):`). release-please builds the changelog and the version from these, so the prefix is not cosmetic. Do not include a body of text in a commit message. Only ever single line commits.

## Build & Development Commands

## Architecture

Kotlin Multiplatform with Compose Multiplatform UI. 
One shared module (`shared/`) holds essentially the whole app — UI, domain and data; 
the three app modules (`androidApp/`, `desktopApp/`, `iosApp/`) are thin platform shells that build an `AppContainer` and hand it to `App()`.

- **`domain/`** — pure Kotlin. Models, repository *interfaces*, service interfaces, and the `Result`/`Error` types. Knows nothing about Room, the network, or any platform type.
- **`data/`** — implementations. `data/local/` is Room + DataStore; `data/repositories/` holds the implementations, named after their backing store (`OfflineItemRepository` = Room-backed).
- **`ui/`** — Compose. `ui/pages/<feature>/` is one directory per screen containing `XPage.kt` + `XViewModel.kt`; `ui/navigation/` holds the route definitions and the single NavHost; `ui/components/` is anything reused across pages; `ui/theme/` is colour, typography and the theme wrapper.

**Key architectural decisions:**

- **Manual dependency injection** `AppContainer` is constructed once per platform in the platform entry point and threaded down explicitly.
- **ViewModels are constructed in the nav host**, inside each `composable<Route>` block, from the `AppContainer`.
- **Reactive state.** Repositories expose `Flow`; ViewModels convert with `stateIn(SharingStarted.WhileSubscribed(5000))`; pages read with `collectAsStateWithLifecycle()`.
- **Type-safe navigation.** Routes are `@Serializable` types in a sealed class, not strings. Arguments are read with `backStackEntry.toRoute<T>()`.
- **Fallible operations return `Result<D, E>`,** they do not throw. Repositories catch storage exceptions and map them to a `DataError`. Read streams stay unwrapped.

## {{TODO: domain deep-dive}}

<!--
  Replace this heading with the part of THIS app that is most likely to bite
  someone — the sync protocol, the parsing pipeline, the device/BLE state machine.
  Describe the flow in numbered steps and name the reference implementation for
  each. This is the highest-value section in the file; do not leave it empty.
-->

## File Map

All paths relative to `shared/src/commonMain/kotlin/{{PACKAGE_PATH}}/` unless stated otherwise. Only non-obvious files are annotated.

**App entry & DI:**
- `AppContainer.kt` — manual DI container. **Registration point** for every new repository or service.
- `App.kt` — shared composable root: theme + nav host.
- `../../androidMain/.../MyApplication.kt` — Android composition root; owns `appScope` for work that must outlive a screen.
- `../../jvmMain/.../AppContainer.jvm.kt` — `createAppContainer()`, the desktop composition root.
- `../../iosMain/.../MainViewController.kt` — the iOS composition root.
- `androidApp/src/main/kotlin/.../MainActivity.kt` — reads the container off the Application and calls `App()`.

**Domain (`domain/`):**
- `Result.kt` — `Result.Success` / `Result.Error`; the return type of every fallible call.
- `Error.kt` — the `Error` marker interface plus `DataError`. `getResource()` maps an error onto the user-facing string. **Registration point** for new error categories.
- `repositories/` — interfaces only.

**Data — persistence (`data/local/`):**
- `AppDatabase.kt` — Room database + the `expect object AppDatabaseConstructor`. **Registration point**: new entities and DAOs are declared here, and the version bump needs a migration.
- `Entities.kt` — every Room entity in the app, in one file.
- `Converters.kt` — Room type converters.
- `createDataStore.kt` — shared DataStore factory; the per-platform path providers are the `Database.<platform>.kt` files in `androidMain`/`iosMain`/`jvmMain`.

**Data — repositories (`data/repositories/`):**
- `OfflineItemRepository.kt` — reference implementation: entity↔domain mapping, exception→`Result.Error` conversion.
- `OfflinePreferencesRepository.kt` — DataStore-backed settings.

**UI — navigation (`ui/navigation/`):**
- `PageNavigation.kt` — the `@Serializable` sealed route hierarchy. **Registration point** for every new screen.
- `ApplicationNavigationHost.kt` — the single NavHost; **the only place ViewModels are constructed**.

**UI — pages (`ui/pages/`):** one directory per screen, each with `XPage.kt` + `XViewModel.kt`.

**UI — components (`ui/components/`):**
- `PageShell.kt` — the Scaffold wrapper every page uses; owns the app's standard padding.
- `Dialogs.kt` — all shared dialogs, in one file.

**UI — theme (`ui/theme/`):** `Color.kt` (named semantic palette), `Theme.kt` (light + dark schemes, reads the stored preference), `Type.kt` (`appTypography()`, baseline-copy pattern).

**Tests:** `shared/src/jvmTest/` — `StringResourceCompletenessTest.kt` verifies every locale against the default strings.

## Registration Points (Adding New Things)

**A new screen:**
1. Add a route to `PageNavigation.kt` (an `object`, or a `data class` if it takes arguments).
2. Add a `composable<PageNavigation.X>` block in `ApplicationNavigationHost.kt` and build the ViewModel there from the `AppContainer`.
3. Create `ui/pages/<name>/XPage.kt` + `XViewModel.kt`. Wrap the page body in `PageShell`.
4. Add its strings to `values/strings.xml` **and every `values-XX/strings.xml`**.

**New persisted data:**
1. Add the entity to `Entities.kt` and a DAO to `data/local/`.
2. Register both on `AppDatabase` (`entities = [...]`, plus the abstract DAO getter).
3. Bump `version` **and write the migration**.
4. Add the repository interface in `domain/repositories/`, the implementation in `data/repositories/`, and expose it from `AppContainer`.

**A new repository or service:**
1. Interface in `domain/repositories/` (or `domain/services/`).
2. Implementation in `data/repositories/` (or `data/services/`), named after its backing store.
3. Expose it on `AppContainer` — `by lazy` if construction is expensive.

**A platform-specific service:** declare the interface in `domain/services/`, then implement it once per platform in `androidMain`/`iosMain`/`jvmMain` and pass it into the `AppContainer` constructor from each composition root (all three must be updated).

## Key Conventions

- **ViewModel state**: expose `StateFlow`, built with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`. Read it in composables with `collectAsStateWithLifecycle()`.
- **Error handling**: services return `Result`, never throw across a layer boundary. ViewModels surface failures as state the page renders.
- **Pages** take `navController` and `viewModel` as named parameters and hold no state beyond pure UI state (dialog open/closed).
- **Strings**: always `stringResource(Res.string.x)`.
