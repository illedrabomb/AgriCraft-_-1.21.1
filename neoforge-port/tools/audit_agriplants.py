#!/usr/bin/env python3
"""Inventory a pinned AgriPlants checkout without modifying source or port files.

Usage:
  python3 tools/audit_agriplants.py --source /path/to/AgriPlants --report /path/to/audit.json

The report classifies legacy semantics, not whether Minecraft 1.21.1 supports
them. Review all flagged rules and existing cropgenetics adapters manually.
This is an offline, standard-library-only inventory tool.
"""
import argparse
import json
from collections import Counter
from pathlib import Path


def classify_crop(document):
    requirements = document.get("requirement") or {}
    texture = document.get("texture") or {}
    seeds = document.get("seed_items") or []
    products = (document.get("products") or {}).get("products") or []
    flags = set()
    for key in ("soil_humidity", "soil_acidity", "soil_nutrients",
                "biomes", "dimensions", "seasons", "conditions", "fluid"):
        value = requirements.get(key)
        if value not in (None, [], {}):
            # Empty blacklist rules are still potentially semantically relevant.
            flags.add("legacy_requirement:" + key)
    if document.get("callbacks"):
        flags.add("callbacks")
    if (document.get("clip_products") or {}).get("products"):
        flags.add("clip_products")
    if document.get("structure_generation"):
        flags.add("structure_generation")
    if document.get("particle_effects"):
        flags.add("particle_effects")
    if any(seed.get("overridePlanting") for seed in seeds if isinstance(seed, dict)):
        flags.add("override_planting")
    if any(seed.get("useTag") or seed.get("nbt") for seed in seeds if isinstance(seed, dict)):
        flags.add("custom_seed_item")
    if any(p.get("useTag") or p.get("nbt") or p.get("required") is False
           for p in products if isinstance(p, dict)):
        flags.add("custom_product")
    if any(document.get(key, default) != default for key, default in
           (("grass_drop_chance", 0), ("seed_drop_chance", 1),
            ("seed_drop_bonus", 0))):
        flags.add("custom_seed_drops")
    geometry = texture.get("render_type", "unknown")
    if geometry not in ("hash", "plus"):
        flags.add("special_geometry:" + str(geometry))
    if any(isinstance(stage, list) and len(stage) > 1
           for stage in texture.get("plant_textures", []) or []):
        flags.add("multilayer_visual")
    if any(isinstance(t, str) and t.startswith("agricraft:")
           for stage in texture.get("plant_textures", []) or []
           for t in (stage if isinstance(stage, list) else [stage])):
        flags.add("legacy_asset_dependency")
    if any(m not in ("agricraft", "minecraft") for m in document.get("mods", [])):
        flags.add("mod_dependency")
    return sorted(flags), geometry


def audit(source):
    if not source.is_dir():
        raise ValueError("source must be an existing AgriPlants directory")
    results = []
    counts = Counter()
    errors = []
    # Some mod integrations are in their own folders; discover all plant
    # directories without assuming a single namespace or version layout.
    candidates = sorted(path for path in source.rglob("*.json")
                        if "plants" in path.relative_to(source).parts)
    for path in candidates:
        relative = path.relative_to(source).as_posix()
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
            if not isinstance(data, dict) or not isinstance(data.get("id"), str):
                raise ValueError("not a plant definition with string id")
            flags, geometry = classify_crop(data)
            for flag in flags:
                counts[flag] += 1
            results.append({"source": relative, "id": data["id"],
                            "enabled": data.get("enabled", True),
                            "mods": data.get("mods", []),
                            "geometry": geometry, "flags": flags})
        except (ValueError, OSError, TypeError, KeyError) as exc:
            errors.append({"source": relative, "error": str(exc)})
    return {"schema_version": 1, "source": str(source.resolve()),
            "plant_files": len(results), "unreadable_files": errors,
            "flag_counts": dict(sorted(counts.items())), "plants": results}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", required=True, type=Path)
    parser.add_argument("--report", required=True, type=Path)
    args = parser.parse_args()
    report = audit(args.source)
    body = json.dumps(report, indent=2, ensure_ascii=False) + "\n"
    # Keep the tool read-only unless a report destination is explicitly chosen;
    # never overwrite existing reports.
    if args.report.exists() or args.report.is_symlink():
        parser.error("report already exists; choose a new path")
    args.report.parent.mkdir(parents=True, exist_ok=True)
    with args.report.open("x", encoding="utf-8") as output:
        output.write(body)
    print(f"Inventoried {report['plant_files']} crops; "
          f"{len(report['unreadable_files'])} unreadable files; report: {args.report}")


if __name__ == "__main__":
    main()
