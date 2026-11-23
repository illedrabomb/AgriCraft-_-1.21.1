#!/usr/bin/env bash
# Explicitly opted-in, core-only automated verification. Never invoked by CI.
set -Eeuo pipefail

if [[ ${1:-} != '--approve-core-tests' || $# -ne 1 ]]; then
  echo 'GESPERRT: Nur nach ausdruecklicher Freigabe: bash scripts/core-test-gate.sh --approve-core-tests' >&2
  exit 2
fi

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

if [[ ! -f build.gradle || ! -f settings.gradle ]]; then
  echo 'FEHLER: Hauptmod-Projektverzeichnis nicht gefunden.' >&2
  exit 2
fi

if [[ -f gradlew ]]; then
  gradle_cmd=(bash ./gradlew)
elif command -v gradle >/dev/null 2>&1; then
  gradle_cmd=(gradle)
else
  echo 'FEHLER: Kein Gradle-Wrapper im Repository und kein gradle im PATH. Nichts ausgefuehrt.' >&2
  exit 2
fi

if ! command -v java >/dev/null 2>&1; then
  echo 'FEHLER: Java nicht gefunden. Nichts ausgefuehrt.' >&2
  exit 2
fi
java_version="$(java -version 2>&1 | head -n 1)"
if [[ "$java_version" != *'"21.'* && "$java_version" != *'"21"'* && "$java_version" != *'openjdk 21 '* ]]; then
  echo "FEHLER: Java 21 erforderlich, gefunden: $java_version. Nichts ausgefuehrt." >&2
  exit 2
fi

if ! command -v git >/dev/null 2>&1; then
  echo 'FEHLER: Git nicht gefunden. Nichts ausgefuehrt.' >&2
  exit 2
fi

branch="$(git branch --show-current)"
if [[ "$branch" != 'integration/neoforge-1211-consolidation' ]]; then
  echo "FEHLER: Falscher Branch: $branch. Nichts ausgefuehrt." >&2
  exit 2
fi
if [[ -n "$(git status --porcelain)" ]]; then
  echo 'FEHLER: Arbeitsbaum nicht sauber. Erst committen und GitHub-HEAD abgleichen.' >&2
  exit 2
fi

# This probe reads Gradle version only; it never starts build tasks.
# Gradle must support running under Java 21 (Gradle >= 8.5).
if ! gradle_version_output="$("${gradle_cmd[@]}" --version 2>&1)"; then
  echo 'FEHLER: Gradle-Version nicht lesbar. Keine Tests oder Build-Tasks gestartet.' >&2
  exit 2
fi
gradle_version="$(printf '%s\n' "$gradle_version_output" |
  awk '/^Gradle [0-9]+\.[0-9]+/ { print $2; exit }')"
if [[ ! "$gradle_version" =~ ^([0-9]+)\.([0-9]+)(\.[0-9]+)?$ ]]; then
  echo "FEHLER: Ungueltige Gradle-Version: $gradle_version. Nichts ausgefuehrt." >&2
  exit 2
fi
gradle_major="${BASH_REMATCH[1]}"
gradle_minor="${BASH_REMATCH[2]}"
if (( gradle_major < 8 || (gradle_major == 8 && gradle_minor < 5) )); then
  echo "FEHLER: Gradle $gradle_version kann nicht unter Java 21 laufen (mindestens 8.5 erforderlich). Alten Forge-7.4-Wrapper nicht verwenden." >&2
  exit 2
fi

sha="$(git rev-parse HEAD)"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
report_dir="$project_dir/build/core-gate-reports/$timestamp-$sha"
mkdir -p "$report_dir"
touch "$report_dir/run-start.marker"
{
  echo "UTC=$timestamp"
  echo "BRANCH=$branch"
  echo "COMMIT=$sha"
  echo "JAVA=$java_version"
  echo "GRADLE=$gradle_version"
  echo 'SCOPE=core-only :test :build (no Minecraft run, no addon tasks)'
  echo 'APPROVAL=explicit CLI flag; user approval must exist separately'
} > "$report_dir/manifest.txt"

set +e
"${gradle_cmd[@]}" :test :build --no-daemon --console=plain \
  > "$report_dir/gradle.log" 2>&1
result=$?
set -e
echo "$result" > "$report_dir/exit-code.txt"
if command -v python3 >/dev/null 2>&1; then
  python3 scripts/summarize-core-gate.py "$report_dir" || \
    echo 'WARNUNG: Ergebniszusammenfassung fehlgeschlagen; Rohlogs sind erhalten.' >&2
else
  echo 'HINWEIS: python3 fehlt; Rohlogs sind erhalten, keine Kurzfassung.' >&2
fi
echo "Protokoll: $report_dir"
if [[ $result -ne 0 ]]; then
  echo 'NICHT BESTANDEN: Gradle-Log und Testberichte auswerten; kein Minecraft-Test freigegeben.' >&2
else
  echo 'Gradle-Aufgaben abgeschlossen; Berichte/JAR noch manuell zu kontrollieren. Kein Minecraft-Test.'
fi
exit "$result"
