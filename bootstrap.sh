#!/usr/bin/env bash
#
# Turns this template into a real project: rewrites the package name, the Gradle
# project name, the Android applicationId, the iOS bundle identifier and the
# Compose-resources package, moves the source directories to match, optionally
# generates locale files, replaces this template's README with an empty one,
# deletes itself and commits the result.
#
# The existing git history, branch and remote are left alone — bootstrapping is
# recorded as a single "chore: run bootstrap.sh" commit on top of whatever the
# repository already has. Nothing is pushed.
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
COMMIT=1
WITH_IOS=1
WITH_DESKTOP=1
DRY_RUN=0

COMMIT_SUBJECT="chore: run bootstrap.sh"

die() { echo "error: $*" >&2; exit 1; }

# Prints the header comment block, so the usage text can never drift from it.
usage() {
    awk 'NR == 1 { next } /^#/ { sub(/^# ?/, ""); print; next } { exit }' "$0"
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
        --no-commit)  COMMIT=0; shift ;;
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

# ------------------------------------------------------------- git preflight --

# Decided up front so --dry-run can report it, and so a missing git or a dirty
# tree is mentioned before anything on disk has been touched.
GIT_COMMIT=0
GIT_NOTE=""

if [[ $COMMIT == 1 ]]; then
    if ! command -v git >/dev/null 2>&1; then
        GIT_NOTE="git not found"
    elif ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        GIT_NOTE="not a git repository"
    else
        GIT_COMMIT=1
    fi
fi

echo "  package        : $TEMPLATE_PACKAGE -> $PACKAGE"
echo "  applicationId  : $APP_ID"
echo "  rootProject    : $TEMPLATE_ROOT_NAME -> $ROOT_NAME"
echo "  resources pkg  : $TEMPLATE_RES_PKG -> $RES_PKG"
echo "  display name   : $DISPLAY_NAME"
echo "  database file  : $DB_NAME"
[[ -n "$LOCALES" ]] && echo "  locales        : $LOCALES"
[[ $WITH_IOS     == 0 ]] && echo "  iOS            : REMOVED"
[[ $WITH_DESKTOP == 0 ]] && echo "  desktop        : REMOVED"

if [[ $GIT_COMMIT == 1 ]]; then
    echo "  git            : commit \"$COMMIT_SUBJECT\" (no push)"
else
    echo "  git            : leaving the changes uncommitted${GIT_NOTE:+ ($GIT_NOTE)}"
fi
echo

# Everything staged by the commit below is whatever is in the tree at that
# point, so pre-existing edits would be folded in. Say so while it is still
# possible to abort.
if [[ $GIT_COMMIT == 1 && -n "$(git status --porcelain)" ]]; then
    echo "warning: the working tree is not clean — the changes below will be"
    echo "         included in the bootstrap commit:"
    git status --short | sed 's/^/           /'
    echo
fi

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

# Doc placeholders. README.md is not listed: it is replaced wholesale below.
for doc in AGENTS.md CLAUDE.md; do
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

# The template's own README describes the template, which is noise in a real
# project — throw it away and start from an empty one. printf rather than a
# heredoc so a "$" or a backtick in the display name survives verbatim.
printf '# %s\n' "$DISPLAY_NAME" > README.md

cat > CHANGELOG.md <<EOF
# Changelog
EOF

rm -f bootstrap.sh

# ------------------------------------------------------------------ committing --

# The script has just deleted itself; on Linux the running shell keeps reading
# from the open inode, so the rest of this file still executes. "git add -A"
# stages that deletion along with everything else.
COMMITTED=0
if [[ $GIT_COMMIT == 1 ]]; then
    body="package $PACKAGE, rootProject $ROOT_NAME, applicationId $APP_ID"
    [[ $WITH_IOS     == 0 ]] && body="$body, iOS removed"
    [[ $WITH_DESKTOP == 0 ]] && body="$body, desktop removed"

    git add -A
    if git diff --cached --quiet; then
        echo "Nothing to commit — the tree already matched the requested settings."
    elif git commit -q -m "$COMMIT_SUBJECT" -m "$body"; then
        COMMITTED=1
        branch="$(git branch --show-current)"
        echo "Committed as \"$COMMIT_SUBJECT\"${branch:+ on $branch}. Not pushed."
    else
        echo "warning: git commit failed — the changes are staged, commit them yourself." >&2
    fi
fi

echo
echo "Done. Next:"
echo "  1. ./gradlew :androidApp:assembleDebug"
echo "  2. ./gradlew :desktopApp:run"
echo "  3. fill in the {{TODO}} sections of AGENTS.md, and write README.md"
echo "  4. add the CI secrets: RELEASE_PLEASE_TOKEN, KEYSTORE_BASE64,"
echo "     KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
if [[ $COMMITTED == 1 ]]; then
    echo "  5. review with 'git show', then 'git push'"
else
    echo "  5. review the changes, then commit them"
fi
