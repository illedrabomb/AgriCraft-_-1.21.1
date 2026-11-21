"""Regression tests for the report reader. Written but not executed pre-gate."""

import importlib.util
import os
from pathlib import Path
import tempfile
import unittest


SCRIPT = Path(__file__).with_name("summarize-core-gate.py")
SPEC = importlib.util.spec_from_file_location("summarize_core_gate", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class CoreGateSummaryTests(unittest.TestCase):
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
