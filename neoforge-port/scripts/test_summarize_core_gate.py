"""Regression tests for the report reader. Written but not executed pre-gate."""

import importlib.util
import hashlib
import os
from pathlib import Path
import tempfile
import unittest


SCRIPT = Path(__file__).with_name("summarize-core-gate.py")
SPEC = importlib.util.spec_from_file_location("summarize_core_gate", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class CoreGateSummaryTests(unittest.TestCase):
    def test_windows_manifest_bom_keeps_full_commit_and_scope(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "manifest.txt"
            path.write_text(
                "COMMIT=" + "a" * 40 + "\n"
                "BRANCH=integration/neoforge-1211-consolidation\n"
                "JAVA=openjdk version 21.0.2\n"
                "SCOPE=core-only :test :build\n", encoding="utf-8-sig")
            data = MODULE.read_manifest(path)
            self.assertEqual("a" * 40, data["COMMIT"])
            self.assertEqual([], MODULE.manifest_warnings(data))

    def test_missing_or_malformed_manifest_never_proves_provenance(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "missing.txt"
            self.assertEqual({}, MODULE.read_manifest(path))
            self.assertEqual(4, len(MODULE.manifest_warnings({})))
            path.write_text("COMMIT=not-a-sha\nBRANCH=master\n"
                            "JAVA=\nSCOPE=addon-only\n", encoding="utf-8")
            self.assertEqual(4, len(MODULE.manifest_warnings(MODULE.read_manifest(path))))

    def test_zero_exit_does_not_hide_missing_test_and_jar_evidence(self):
        warnings = MODULE.evidence_warnings(
            "0", [], dict(tests=0, failures=0, errors=0, skipped=0), [], [])
        self.assertEqual(3, len(warnings))
        self.assertTrue(any("JUnit-XML" in warning for warning in warnings))
        self.assertTrue(any("Testfälle" in warning for warning in warnings))
        self.assertTrue(any("JAR" in warning for warning in warnings))

    def test_gradle_failure_and_all_skipped_tests_stay_visible(self):
        warnings = MODULE.evidence_warnings(
            "1", [Path("TEST-skipped.xml")],
            dict(tests=3, failures=0, errors=0, skipped=3), [], [Path("core.jar")])
        self.assertEqual(2, len(warnings))
        self.assertTrue(any("Gradle" in warning for warning in warnings))
        self.assertTrue(any("übersprungen" in warning for warning in warnings))

    def test_formal_evidence_yields_no_false_warning(self):
        self.assertEqual([], MODULE.evidence_warnings(
            "0", [Path("TEST-core.xml")],
            dict(tests=3, failures=0, errors=0, skipped=1), [], [Path("core.jar")]))

    def test_artifact_fingerprint_matches_bytes_without_modifying_file(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "agricraft-fixture.jar"
            payload = b"sample jar contents for fingerprint source test"
            path.write_bytes(payload)
            before = path.stat()
            self.assertEqual(hashlib.sha256(payload).hexdigest(), MODULE.sha256_file(path))
            self.assertEqual(payload, path.read_bytes())
            self.assertEqual(before.st_mtime_ns, path.stat().st_mtime_ns)

    def test_ignores_stale_xml_and_does_not_invent_passes(self):
        with tempfile.TemporaryDirectory() as directory:
            old = Path(directory) / "TEST-old.xml"
            old.write_text('<testsuite tests="900" failures="0"/>', encoding="utf-8")
            os.utime(old, ns=(100, 100))
            paths, totals, issues, broken = MODULE.collect_results(Path(directory), 200)
            self.assertEqual([], paths)
            self.assertEqual(0, totals["tests"])
            self.assertEqual([], issues)
            self.assertEqual([], broken)

    def test_reports_fresh_failures_and_skips(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "TEST-regression.xml"
            path.write_text('''<testsuite tests="3" failures="1" errors="0" skipped="1">
              <testcase classname="CropTest" name="fails">
                <failure message="expected seed"/>
              </testcase>
              <testcase classname="CropTest" name="passes"/>
              <testcase classname="CropTest" name="skips"><skipped/></testcase>
            </testsuite>''', encoding="utf-8")
            paths, totals, issues, broken = MODULE.collect_results(Path(directory), 0)
            self.assertEqual([path], paths)
            self.assertEqual({"tests": 3, "failures": 1, "errors": 0, "skipped": 1}, totals)
            self.assertEqual([("CropTest", "fails", "failure", "expected seed", path.name)], issues)
            self.assertEqual([], broken)

    def test_malformed_xml_is_not_treated_as_a_success(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "TEST-broken.xml"
            path.write_text("<testsuite", encoding="utf-8")
            paths, totals, issues, broken = MODULE.collect_results(Path(directory), 0)
            self.assertEqual([path], paths)
            self.assertEqual(0, totals["tests"])
            self.assertEqual([], issues)
            self.assertEqual("TEST-broken.xml", broken[0][0])


if __name__ == "__main__":
    unittest.main()
