#!/usr/bin/env python3
"""Summarize existing Gradle/JUnit evidence; never starts Minecraft or tests."""

import argparse
import hashlib
from pathlib import Path
import re
import xml.etree.ElementTree as ET


EXPECTED_BRANCH = "integration/neoforge-1211-consolidation"


def read_manifest(path):
    """Read only plain, single-line gate metadata (Windows UTF-8 BOM accepted)."""
    if not path.is_file():
        return {}
    data = {}
    for line in path.read_text(encoding="utf-8-sig").splitlines():
        key, separator, value = line.partition("=")
        if separator and key in {"COMMIT", "BRANCH", "JAVA", "GRADLE", "SCOPE"}:
            data[key] = value.strip()
    return data


def manifest_warnings(data):
    """Reject missing or invalid provenance instead of attributing a JAR."""
    warnings = []
    if not re.fullmatch(r"[0-9a-f]{40}", data.get("COMMIT", "")):
        warnings.append("Git-Commit im Laufmanifest fehlt oder ist ungültig.")
    if data.get("BRANCH") != EXPECTED_BRANCH:
        warnings.append("Entwicklungsbranch im Laufmanifest fehlt oder stimmt nicht.")
    if not data.get("JAVA"):
        warnings.append("Java-Version im Laufmanifest fehlt.")
    gradle = re.fullmatch(r"([0-9]+)\.([0-9]+)(?:\.[0-9]+)?",
                          data.get("GRADLE", ""))
    if gradle is None:
        warnings.append("Gradle-Version im Laufmanifest fehlt oder ist ungültig.")
    elif (int(gradle.group(1)), int(gradle.group(2))) < (8, 5):
        warnings.append("Gradle-Version unterstützt den Java-21-Lauf nicht (mindestens 8.5).")
    if not data.get("SCOPE", "").startswith("core-only "):
        warnings.append("Core-only-Umfang im Laufmanifest fehlt oder stimmt nicht.")
    return warnings


def sha256_file(path):
    """Fingerprint existing artifacts in bounded chunks; never modify them."""
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def collect_results(xml_dir, marker_ns):
    """Return only reports written since the current gate began."""
    fresh_xml = sorted(p for p in xml_dir.glob("TEST-*.xml")
                       if p.stat().st_mtime_ns >= marker_ns)
    totals = dict(tests=0, failures=0, errors=0, skipped=0)
    issues = []
    invalid_xml = []
    for path in fresh_xml:
        try:
            root = ET.parse(path).getroot()
            suites = [root] if root.tag == "testsuite" else root.findall("testsuite")
            if not suites:
                raise ValueError("no testsuite element")
            for suite in suites:
                for key in totals:
                    totals[key] += int(suite.attrib.get(key, "0"))
                for case in suite.findall("testcase"):
                    for kind in ("failure", "error"):
                        failure = case.find(kind)
                        if failure is not None:
                            title = failure.get("message", "").replace("\n", " ").strip()
                            issues.append((case.get("classname", "?"), case.get("name", "?"),
                                           kind, title[:200], path.name))
        except (ET.ParseError, ValueError) as exc:
            invalid_xml.append((path.name, str(exc)))
    return fresh_xml, totals, issues, invalid_xml


def evidence_warnings(code, fresh_xml, totals, invalid_xml, jars):
    """Identify absent or contradictory evidence; never declare a test pass."""
    warnings = []
    if code != "0":
        warnings.append("Gradle-Exitcode ist nicht 0; Aufgabenlauf fehlgeschlagen oder unklar.")
    if not fresh_xml:
        warnings.append("Keine frischen JUnit-XML-Dateien vorhanden.")
    if invalid_xml:
        warnings.append("Mindestens ein frischer JUnit-Bericht ist defekt oder ungültig.")
    if totals["tests"] <= 0:
        warnings.append("Keine ausgeführten JUnit-Testfälle nachgewiesen.")
    elif totals["skipped"] >= totals["tests"]:
        warnings.append("Alle berichteten JUnit-Testfälle wurden übersprungen.")
    if totals["failures"] or totals["errors"]:
        warnings.append("JUnit meldet Fehler oder fehlgeschlagene Testfälle.")
    if not jars:
        warnings.append("Kein frisch erzeugtes JAR-Artefakt nachgewiesen.")
    return warnings


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report_dir", type=Path)
    args = parser.parse_args()
    report_dir = args.report_dir.resolve()
    marker = report_dir / "run-start.marker"
    exit_code = report_dir / "exit-code.txt"
    if not marker.is_file() or not exit_code.is_file():
        parser.error("missing run marker or exit code: not a completed gate run")

    project_dir = Path(__file__).resolve().parent.parent
    xml_dir = project_dir / "build" / "test-results" / "test"
    fresh_xml, totals, issues, invalid_xml = collect_results(
        xml_dir, marker.stat().st_mtime_ns)

    code = exit_code.read_text(encoding="utf-8").strip()
    manifest = read_manifest(report_dir / "manifest.txt")
    jars = sorted(p for p in (project_dir / "build" / "libs").glob("*.jar")
                  if p.stat().st_mtime_ns >= marker.stat().st_mtime_ns)
    warnings = evidence_warnings(code, fresh_xml, totals, invalid_xml, jars)
    warnings.extend(manifest_warnings(manifest))
    lines = ["# Codex Core-Gate – automatisch extrahierter Befund", "",
             "> Rohdaten-Auswertung, **keine** Ursachenanalyse oder P0/P1-Einstufung.", "",
             "## Beleglage – keine automatische Testfreigabe", "",
             "**WARNUNG: Unvollständiger oder fehlerhafter Gate-Nachweis.**" if warnings
             else "**Nur formale Belege vorhanden – noch kein geprüfter Pass-Status.**",
             ""]
    lines.extend(f"- {warning}" for warning in warnings)
    lines += ["", "## Rohdaten", "",
             f"- Git-Commit aus Laufmanifest: `{manifest.get('COMMIT', 'FEHLT')}` "
             "(JAR-Zuordnung zusätzlich prüfen)",
             f"- Entwicklungsbranch: `{manifest.get('BRANCH', 'FEHLT')}`",
             f"- Java: `{manifest.get('JAVA', 'FEHLT')}`",
             f"- Gradle: `{manifest.get('GRADLE', 'FEHLT')}`",
             f"- Laufumfang: `{manifest.get('SCOPE', 'FEHLT')}`",
             f"- Gradle-Exitcode: `{code}` (0 bedeutet nur Task-Erfolg)",
             f"- Frische JUnit-XML-Dateien: **{len(fresh_xml)}**",
             f"- JUnit-Nachweis: **{'FEHLT' if not fresh_xml else 'VORHANDEN'}** "
             "(kein automatischer Pass-Status)",
             f"- JUnit: {totals['tests']} Tests, {totals['failures']} Failures, "
             f"{totals['errors']} Errors, {totals['skipped']} Skipped",
             f"- Während dieses Laufs erzeugte JAR-Dateien: **{len(jars)}**",
             "- Fehlende oder nicht frisch erzeugte XML/JAR-Dateien sind **kein Erfolgsnachweis**.",
             "", "## JAR-Artefakte", ""]
    for path in jars:
        try:
            lines.append(f"- `{path.name}` – {path.stat().st_size} Bytes, "
                         f"SHA-256: `{sha256_file(path)}`")
        except OSError as exc:
            lines.append(f"- `{path.name}` – **nicht lesbar**, keine Prüfsumme: {exc}")
    if not jars:
        lines.append("- Keine frisch erzeugten JARs nachgewiesen.")
    lines += ["", "## Fehlgeschlagene JUnit-Fälle (ungekürzt in XML)", ""]
    lines.extend(f"- `{cls}.{name}` ({kind}): {message} – `{xml}`"
                 for cls, name, kind, message, xml in issues)
    if not issues:
        lines.append("- Keine Failure/Error-Einträge in den gefundenen frischen XML-Dateien.")
    if invalid_xml:
        lines += ["", "## Nicht lesbare XML-Dateien", ""]
        lines.extend(f"- `{name}`: {reason}" for name, reason in invalid_xml)
        lines.append("- **Nicht bestandssicher:** mindestens ein frischer JUnit-Bericht ist unlesbar.")
    lines += ["", "## Weiteres Vorgehen", "",
              "Codex prüft `gradle.log` und die oben verlinkten XML-Dateien auf Ursachen.",
              "Nur eindeutig kritische P0/P1-Funde minimal selbst beheben; P2/P3 im Chat melden.",
              "Minecraft-/Mineflayer-/Multiplayer-Tests bleiben gesondert freigabepflichtig.", ""]
    target = report_dir / "summary.md"
    target.write_text("\n".join(lines), encoding="utf-8")
    print(target)


if __name__ == "__main__":
    main()
