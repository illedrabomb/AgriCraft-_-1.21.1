#!/usr/bin/env python3
"""Read-only source-audited Pam's HarvestCraft 1 -> HarvestCraft 2 ID crosswalk.

This is deliberately NOT a world converter or a runtime mod adapter.
It never writes Minecraft files, edits worlds, imports data into the game,
or fabricates IDs. No operation is performed unless run explicitly.
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

MANIFEST = Path(__file__).parent / "data" / "pams_harvestcraft_1_to_2_crops.json"
ID = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")

def load_manifest(path: Path = MANIFEST) -> dict:
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("schema_version") != 1:
        raise ValueError("Unsupported crosswalk schema_version")
    rules = data.get("rules")
    if not isinstance(rules, list) or not rules:
        raise ValueError("Crosswalk has no rules")
    seen_crops: set[str] = set()
    seen_sources: set[str] = set()
    seen_targets: set[str] = set()
    for index, rule in enumerate(rules):
        name = rule.get("crop")
        if not isinstance(name, str) or not re.fullmatch(r"[a-z][a-z0-9_]*", name):
            raise ValueError(f"Invalid crop key at rule {index}")
        if name in seen_crops:
            raise ValueError(f"Duplicate crop: {name}")
        seen_crops.add(name)
        if rule.get("automatic") is not False or type(rule.get("metadata")) is not int or rule["metadata"] != 0:
            raise ValueError(f"Unsafe automatic or metadata mapping: {name}")
        source = rule.get("from")
        target = rule.get("to")
        if not isinstance(source, dict) or not isinstance(target, dict):
            raise ValueError(f"Missing namespaces at {name}")
        if set(source) != {"seed", "produce", "block"} or set(target) != {"seed", "produce", "species"}:
            raise ValueError(f"Incomplete source/target at {name}")
        for kind, value in source.items():
            if not isinstance(value, str) or not ID.fullmatch(value) or not value.startswith("harvestcraft:"):
                raise ValueError(f"Invalid source {kind}: {name}")
            if value in seen_sources:
                raise ValueError(f"Ambiguous legacy ID: {value}")
            seen_sources.add(value)
        for kind, value in target.items():
            if not isinstance(value, str) or not ID.fullmatch(value) or not value.startswith("pamhc2crops:"):
                raise ValueError(f"Invalid destination {kind}: {name}")
            if value in seen_targets:
                raise ValueError(f"Ambiguous target ID: {value}")
            seen_targets.add(value)
    return data

def lookup(data: dict, source_id: str, *, metadata: int = 0, has_nbt: bool = False) -> dict | None:
    """Never guess: only exact, metadata-zero, NBT-free seed/produce/block IDs."""
    if metadata != 0 or has_nbt or not ID.fullmatch(source_id):
        return None
    for rule in data["rules"]:
        for source_kind, source_value in rule["from"].items():
            if source_value == source_id:
                target_kind = {"seed": "seed", "produce": "produce", "block": "species"}[source_kind]
                return {
                    "crop": rule["crop"],
                    "source_kind": source_kind,
                    "target_kind": target_kind,
                    "target_id": rule["to"][target_kind],
                    "status": "candidate_not_verified_in_target_registry",
                    "automatic": False
                }
    return None

def main() -> None:
    parser = argparse.ArgumentParser(description="Read-only Pam's 1 -> 2 crop ID crosswalk")
    parser.add_argument("--manifest", type=Path, default=MANIFEST)
    parser.add_argument("--lookup", metavar="HARVESTCRAFT:ITEM_OR_BLOCK")
    parser.add_argument("--metadata", type=int, default=0)
    parser.add_argument("--has-nbt", action="store_true")
    args = parser.parse_args()
    data = load_manifest(args.manifest)
    if args.lookup:
        print(json.dumps(lookup(data, args.lookup, metadata=args.metadata,
                                has_nbt=args.has_nbt), indent=2))
    else:
        print(json.dumps({"validated_rules": len(data["rules"]),
                          "automatic_conversions": 0,
                          "source": data["source"]["repository"],
                          "target": data["target"]["repository"]}, indent=2))

if __name__ == "__main__":
    main()
