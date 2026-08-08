#!/usr/bin/env bash
#
# Turns this template into a real project: rewrites the package name, the Gradle
# project name, the Android applicationId, the iOS bundle identifier and the
# Compose-resources package, moves the source directories to match, optionally
# generates locale files, then deletes itself.
#
# Usage:
#   ./bootstrap.sh --package com.foo.bar --name "My App"
#   ./bootstrap.sh --package com.foo.bar --name "My App" --locales da,de,fr
#   ./bootstrap.sh --package com.foo.bar --name "My App" --no-ios --dry-run
#
set -euo pipefail

TEMPLATE_PACKAGE="com.mattschoe.apptemplate"
TEMPLATE_ROOT_NAME="AppTemplate"
TEMPLATE_RES_PKG="apptemplate"
TEMPLATE_DISPLAY_NAME="App Template"
TEMPLATE_DB_NAME="apptemplate.db"

PACKAGE=""
DISPLAY_NAME=""
APP_ID=""
LOCALES=""
KEEP_GIT=0
WITH_IOS=1
WITH_DESKTOP=1
DRY_RUN=0

die() { echo "error: $*" >&2; exit 1; }

usage() {
    sed -n '2,14p' "$0" | sed 's/^# \{0,1\}//'
    exit "${1:-0}"
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --package)   PACKAGE="${2:-}"; shift 2 ;;
        --name)      DISPLAY_NAME="${2:-}"; shift 2 ;;
        --app-id)    APP_ID="${2:-}"; shift 2 ;;
        --locales)   LOCALES="${2:-}"; shift 2 ;;
        --no-ios)     WITH_IOS=0; shift ;;
        --no-desktop) WITH_DESKTOP=0; shift ;;
        --keep-git)   KEEP_GIT=1; shift ;;
        --dry-run)    DRY_RUN=1; shift ;;
        -h|--help)    usage 0 ;;
        *) die "unknown argument: $1 (try --help)" ;;
    esac
done

# ---------------------------------------------------------------- validation --

[[ -n "$PACKAGE" ]]      || die "--package is required (e.g. com.foo.bar)"
[[ -n "$DISPLAY_NAME" ]] || die "--name is required (e.g. \"My App\")"

[[ -f "settings.gradle.kts" && -f "bootstrap.sh" && -d "shared" ]] \
    || die "run this from the template repository root"

[[ "$PACKAGE" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]] \
    || die "--package must be a lowercase dotted identifier, e.g. com.foo.bar"

# rootProject.name drives the Compose-resources package, so it must be a single
# alphanumeric token: "My App" -> "MyApp" -> resources package "myapp".
ROOT_NAME="$(printf '%s' "$DISPLAY_NAME" | tr -cd '[:alnum:]')"
[[ -n "$ROOT_NAME" ]] || die "--name must contain at least one alphanumeric character"
[[ "$ROOT_NAME" =~ ^[A-Za-z] ]] || die "--name must start with a letter"

RES_PKG="$(printf '%s' "$ROOT_NAME" | tr '[:upper:]' '[:lower:]')"
DB_NAME="${RES_PKG}.db"
APP_ID="${APP_ID:-$PACKAGE}"
PACKAGE_PATH="${PACKAGE//.//}"
TEMPLATE_PACKAGE_PATH="${TEMPLATE_PACKAGE//.//}"

echo "  package        : $TEMPLATE_PACKAGE -> $PACKAGE"
echo "  applicationId  : $APP_ID"
echo "  rootProject    : $TEMPLATE_ROOT_NAME -> $ROOT_NAME"
echo "  resources pkg  : $TEMPLATE_RES_PKG -> $RES_PKG"
echo "  display name   : $DISPLAY_NAME"
echo "  database file  : $DB_NAME"
[[ -n "$LOCALES" ]] && echo "  locales        : $LOCALES"
[[ $WITH_IOS     == 0 ]] && echo "  iOS            : REMOVED"
[[ $WITH_DESKTOP == 0 ]] && echo "  desktop        : REMOVED"
echo

if [[ $DRY_RUN == 1 ]]; then
    echo "(dry run — nothing written)"
    exit 0
fi

# ------------------------------------------------------------ module removal --

if [[ $WITH_IOS == 0 ]]; then
    rm -rf iosApp shared/src/iosMain shared/src/iosTest
    # Drop the iOS job from the release workflow and the guarded target block.
    python3 - <<'PY'
import re, pathlib
p = pathlib.Path(".github/workflows/release-artifact-upload.yml")
if p.exists():
    text = p.read_text()
    text = re.split(r"\n  # iOS —", text)[0].rstrip() + "\n"
    p.write_text(text)
PY
fi

if [[ $WITH_DESKTOP == 0 ]]; then
    rm -rf desktopApp shared/src/jvmMain shared/src/jvmTest
    sed -i '/include(":desktopApp")/d' settings.gradle.kts
fi

# ------------------------------------------------------------- move packages --

while IFS= read -r dir; do
    [[ -d "$dir" ]] || continue
    # Strip the template package path off the end to find the source root
    # (".../src/commonMain/kotlin"). Doing it by suffix rather than by counting
    # "../" keeps this correct for packages of any depth.
    src_root="${dir%/$TEMPLATE_PACKAGE_PATH}"
    [[ "$src_root" != "$dir" ]] || die "could not locate source root for $dir"
    target="$src_root/$PACKAGE_PATH"
    mkdir -p "$target"
    cp -r "$dir/." "$target/"
    rm -rf "$dir"
done < <(find . -type d -path "*/$TEMPLATE_PACKAGE_PATH" -not -path "./build/*")

# Room exports schemas into a directory named after the fully-qualified database
# class, using dots rather than slashes — so the package move above does not catch it.
if [[ -d shared/schemas ]]; then
    for schema_dir in shared/schemas/"$TEMPLATE_PACKAGE".*; do
        [[ -d "$schema_dir" ]] || continue
        mv "$schema_dir" "shared/schemas/${PACKAGE}${schema_dir#shared/schemas/$TEMPLATE_PACKAGE}"
    done
fi

# Remove now-empty leftovers of the old package path (com/mattschoe, com, ...).
find . -depth -type d -empty -not -path "./.git/*" -not -path "./build/*" -delete 2>/dev/null || true

# ------------------------------------------------------------ text rewriting --

# Order matters: the resources package and the DB filename both contain the
# template's short name, so they are rewritten before the broader identifiers.
mapfile -t FILES < <(grep -rl -e "$TEMPLATE_PACKAGE" \
                              -e "$TEMPLATE_ROOT_NAME" \
                              -e "$TEMPLATE_RES_PKG" \
                              -e "$TEMPLATE_DISPLAY_NAME" \
                              . \
                        --exclude-dir=.git \
                        --exclude-dir=build \
                        --exclude-dir=.gradle \
                        --exclude-dir=.kotlin \
                        --exclude-dir=.idea \
                        --exclude=bootstrap.sh 2>/dev/null || true)

for f in "${FILES[@]}"; do
    [[ -f "$f" ]] || continue
    sed -i \
        -e "s|${TEMPLATE_RES_PKG}\.shared\.generated\.resources|${RES_PKG}.shared.generated.resources|g" \
        -e "s|${TEMPLATE_DB_NAME}|${DB_NAME}|g" \
        -e "s|${TEMPLATE_PACKAGE}|${PACKAGE}|g" \
        -e "s|${TEMPLATE_DISPLAY_NAME}|${DISPLAY_NAME}|g" \
        -e "s|${TEMPLATE_ROOT_NAME}|${ROOT_NAME}|g" \
        "$f"
done

# applicationId is the one identifier that may differ from the Kotlin package.
sed -i "s|applicationId = \"${PACKAGE}\"|applicationId = \"${APP_ID}\"|" androidApp/build.gradle.kts

# Swap the template's own README (which explains how to use the template) for a
# README about the project being created.
if [[ -f docs/PROJECT_README.md ]]; then
    mv docs/PROJECT_README.md README.md
fi

# Doc placeholders.
for doc in CLAUDE.md README.md; do
    [[ -f "$doc" ]] || continue
    sed -i -e "s|{{PACKAGE_PATH}}|${PACKAGE_PATH}|g" \
           -e "s|{{PACKAGE}}|${PACKAGE}|g" \
           -e "s|{{DISPLAY_NAME}}|${DISPLAY_NAME}|g" \
           "$doc"
done

# ---------------------------------------------------------------- localisation --

if [[ -n "$LOCALES" ]]; then
    default_strings="shared/src/commonMain/composeResources/values/strings.xml"
    IFS=',' read -ra locale_list <<< "$LOCALES"
    for locale in "${locale_list[@]}"; do
        locale="$(printf '%s' "$locale" | tr -d '[:space:]')"
        [[ -n "$locale" ]] || continue
        dir="shared/src/commonMain/composeResources/values-${locale}"
        mkdir -p "$dir"
        if [[ ! -f "$dir/strings.xml" ]]; then
            # Seeded from the default so StringResourceCompletenessTest starts green;
            # translate the values in place, never add or remove keys.
            sed "s|^<!--|<!-- ${locale}: seeded from the default, awaiting translation. -->\n<!--|" \
                "$default_strings" > "$dir/strings.xml"
        fi
    done
fi

# ------------------------------------------------------------------ finishing --

# AGENTS.md is a copy of CLAUDE.md so the two can never drift.
[[ -f CLAUDE.md ]] && cp CLAUDE.md AGENTS.md

cat > CHANGELOG.md <<EOF
# Changelog
EOF

rm -f bootstrap.sh
rm -rf docs/optional-ci/.keep

if [[ $KEEP_GIT == 0 ]]; then
    rm -rf .git
    git init -q
    git add -A
    git commit -q -m "chore: initial commit from kmp-app-template"
    echo "Initialised a fresh git repository."
fi

echo
echo "Done. Next:"
echo "  1. ./gradlew :androidApp:assembleDebug"
echo "  2. ./gradlew :desktopApp:run"
echo "  3. fill in the {{TODO}} sections of CLAUDE.md and README.md"
echo "  4. add the CI secrets listed in README.md → CI/CD"
