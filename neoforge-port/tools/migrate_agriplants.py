#!/usr/bin/env python3
"""Conservative offline migration of selected AgriPlants 1.18.2 crop records.

Usage:
  python3 tools/migrate_agriplants.py \
    --source /path/to/AgriPlants \
    --output src/main/resources \
    --manifest tools/agriplants_migration_manifest.json

The manifest explicitly opts into every migrated crop and its substituted soil
and light rules. Unsupported semantics stop a record; they are never dropped
silently. Preflight checks every record before writing any resources, and
existing output files are never overwritten. No dependencies beyond the Python standard library.
"""
import argparse
import json
import math
import re
from pathlib import Path

ID = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")

def check_id(value, label):
    if not isinstance(value, str) or not ID.fullmatch(value):
        raise ValueError(f"invalid {label}: {value!r}")
    return value

def plain_item(entry, label):
    if entry.get("type", "item") != "item" or entry.get("useTag", False):
        raise ValueError(f"{label}: tag/non-item entry requires adapter")
    # The legacy parser currently does not support custom item NBT.
    if entry.get("nbt", ""):
        raise ValueError(f"{label}: custom NBT requires adapter")
    return check_id(entry["object"], label)

def finite_chance(value, label):
    if type(value) not in (int, float) or not math.isfinite(value) or not 0 <= value <= 1:
        raise ValueError(f"{label}: chance outside 0..1")
    return value

def location_lists(config):
    """Explicit 1.21.1 ID lists, never guessed from legacy blacklist logic."""
    fields = ("allowed_biomes", "blocked_biomes",
              "allowed_dimensions", "blocked_dimensions")
    result = {}
    for field in fields:
        value = config.get(field, [])
        if not isinstance(value, list) or len(value) > 64:
            raise ValueError(f"{field}: expected up to 64 identifiers")
        for entry in value:
            check_id(entry, field)
        if len(value) != len(set(value)):
            raise ValueError(f"{field}: duplicate identifier")
        if value:
            result[field] = value
    if set(result.get("allowed_biomes", [])) & set(result.get("blocked_biomes", [])):
        raise ValueError("biome allow/block lists overlap")
    if set(result.get("allowed_dimensions", [])) & set(result.get("blocked_dimensions", [])):
        raise ValueError("dimension allow/block lists overlap")
    return result

def convert_crop(source, config):
    crop_id = check_id(source["id"], "plant ID")
    if not source.get("enabled", True):
        raise ValueError("plant disabled upstream")
    callbacks = source.get("callbacks", [])
    if callbacks and (callbacks != ["agricraft:thorns"]
                      or config.get("adapt_thorns") is not True):
        raise ValueError("unsupported callbacks; agricraft:thorns needs adapt_thorns=true")
    if source.get("particle_effects"):
        raise ValueError("legacy particle effects require adapter")
    if source.get("clip_products", {}).get("products"):
        raise ValueError("clip product rules need adapter")
    # Legacy planting override and village generation have no equivalents in
    # this importer; allow explicit, per-crop compatibility deviations only.
    if any(e.get("overridePlanting", False) for e in source.get("seed_items", [])):
        if not config.get("acknowledge_legacy_seed_override", False):
            raise ValueError("legacy overridePlanting needs explicit approval or adapter")
    if source.get("structure_generation") and not config.get(
            "acknowledge_legacy_structure_generation", False):
        raise ValueError("legacy greenhouse/structure generation needs explicit approval or adapter")
    # Non-default seed drops are actual gameplay rules, not harmless metadata.
    if (source.get("grass_drop_chance", 0) != 0
            or source.get("seed_drop_chance", 1) != 1
            or source.get("seed_drop_bonus", 0) != 0):
        raise ValueError("legacy custom seed/grass drop rules require adapter")

    requirements = source.get("requirement", {})
    if requirements and not config.get("acknowledge_legacy_requirements", False):
        raise ValueError("original soil/seasons/conditions need explicit migration approval")
    for key in ("soil_block", "soil_tag"):
        if key in config:
            check_id(config[key], key)
    if ("soil_block" in config) == ("soil_tag" in config):
        raise ValueError("explicitly set exactly one soil_block or soil_tag")
    min_light = config.get("min_light")
    max_light = config.get("max_light")
    if not all(type(x) is int for x in (min_light, max_light)):
        raise ValueError("explicit min_light and max_light required")
    if not 0 <= min_light <= max_light <= 15:
        raise ValueError("light range invalid (Minecraft 1.21.1 uses 0..15)")
    tolerance = config.get("light_tolerance", 0)
    if type(tolerance) is not int or not 0 <= tolerance <= 15:
        raise ValueError("light_tolerance outside 0..15")

    seeds = [{"type": "item", "object": plain_item(e, "seed"), "useTag": False}
             for e in source.get("seed_items", [])]
    products = []
    for e in source.get("products", {}).get("products", []):
        item = plain_item(e, "harvest product")
        minimum, maximum = e["min"], e["max"]
        if not all(type(x) is int for x in (minimum, maximum)):
            raise ValueError("non-integer harvest counts")
        if not 0 <= minimum <= maximum <= 4096:
            raise ValueError("invalid harvest count")
        if e.get("required") is False:
            raise ValueError("optional/conditional product requires adapter")
        products.append({"type": "item", "object": item, "min": minimum,
                         "max": maximum, "chance": finite_chance(e["chance"], "product")})
    stages = source["stages"]
    if not isinstance(stages, list) or not stages or any(
            type(v) is not int or v < 0 for v in stages):
        raise ValueError("unsupported stages")
    harvest = source["harvestStage"]
    if type(harvest) is not int or not 0 <= harvest < len(stages):
        raise ValueError("invalid harvest stage")

    texture = source.get("texture", {})
    if texture.get("plant_models"):
        raise ValueError("legacy custom plant models require renderer adapter")
    if texture.get("journal_overrides"):
        raise ValueError("legacy journal visual overrides require adapter")
    geometry = texture.get("render_type")
    if geometry not in ("hash", "plus", "gourd"):
        raise ValueError("renderer requires an unsupported geometry")
    layers = texture.get("plant_textures")
    if not isinstance(layers, list) or not 1 <= len(layers) <= 64:
        raise ValueError("missing/too many texture layers")
    textures = []
    for layer in layers:
        if not isinstance(layer, list) or not (1 <= len(layer) <= 2):
            raise ValueError("unsupported number of texture layers")
        if len(layer) == 2 and geometry != "gourd":
            raise ValueError("multi-layer stages supported only for gourd geometry")
        stage = []
        replacements = config.get("texture_replacements", {})
        for original in layer:
            t = check_id(replacements.get(original, original), "texture")
            if not t.split(":", 1)[1].startswith(("block/", "plant/")):
                raise ValueError("texture outside block/ or plant/ needs adapter")
            # Legacy agricraft namespace will not exist in standalone port
            # unless the manifest explicitly supplies 1.21.1 texture assets.
            if t.startswith("agricraft:") and not config.get("acknowledge_agricraft_assets", False):
                raise ValueError("legacy agricraft: texture needs asset approval or replacement")
            stage.append(t)
        textures.append(stage if len(stage) == 2 else stage[0])

    known = {"minecraft", "agricraft"}
    foreign_mods = [m for m in source.get("mods", []) if m not in known]
    if foreign_mods and not config.get("allow_foreign_mods", False):
        raise ValueError(f"foreign mod dependencies require approval: {foreign_mods}")

    requirements_new = {"min_light": min_light, "max_light": max_light,
                        "light_tolerance": tolerance}
    requirements_new.update({k: config[k] for k in ("soil_block", "soil_tag") if k in config})
    requirements_new.update(location_lists(config))
    # Seasonal rules are copied only when deliberately approved per crop.
    # No season provider is bundled in the core; restrictions are inactive
    # until a verified optional integration installs a provider.
    if "allowed_seasons" in config:
        seasons = config["allowed_seasons"]
        if (not isinstance(seasons, list) or len(seasons) > 4
                or any(type(v) is not str or v not in
                       ("spring", "summer", "autumn", "winter") for v in seasons)
                or len(set(seasons)) != len(seasons)):
            raise ValueError("allowed_seasons must contain unique canonical season names")
        if seasons:
            requirements_new["allowed_seasons"] = seasons

    for name in ("min_y", "max_y"):
        if name in config:
            if type(config[name]) is not int or not -2048 <= config[name] <= 2048:
                raise ValueError(f"{name}: expected integer in -2048..2048")
            requirements_new[name] = config[name]
    if ("min_y" in config and "max_y" in config
            and config["min_y"] > config["max_y"]):
        raise ValueError("min_y exceeds max_y")
    if "fluid_id" in config:
        requirements_new["fluid_id"] = check_id(config["fluid_id"], "fluid_id")

    plant = {
        "version": "1.21.1-compat", "enabled": True,
        "mods": list(dict.fromkeys(["minecraft"] + foreign_mods)),
        "id": crop_id, "seed_items": seeds, "stages": stages, "harvestStage": harvest,
        "growth_chance": finite_chance(source["growth_chance"], "growth"),
        "growth_bonus": source["growth_bonus"],
        "cloneable": bool(source["cloneable"]),
        "spread_chance": finite_chance(source["spread_chance"], "spread"),
        "products": {"products": products},
        "cropgenetics_requirements": requirements_new
    }
    if callbacks == ["agricraft:thorns"]:
        plant["cropgenetics_contact_thorns"] = True
    if type(plant["growth_bonus"]) not in (int, float) or not math.isfinite(plant["growth_bonus"]) or plant["growth_bonus"] < 0:
        raise ValueError("invalid growth bonus")
    return plant, {"plant": crop_id, "geometry": {"hash": "cross"}.get(geometry, geometry),
                   "stages": textures}

def output_file(root, relative, document):
    file = root / relative
    file.parent.mkdir(parents=True, exist_ok=True)
    file.write_text(json.dumps(document, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

def safe_target(root, relative):
    """Reject pre-existing targets and paths escaping the output directory."""
    base = root.resolve()
    target = root / relative
    if not target.resolve().is_relative_to(base):
        raise ValueError(f"output path escapes directory: {relative}")
    if target.exists() or target.is_symlink():
        raise ValueError(f"output exists (never overwrite): {relative}")
    return relative

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--source", type=Path, required=True)
    ap.add_argument("--output", type=Path, required=True)
    ap.add_argument("--manifest", type=Path, required=True)
    args = ap.parse_args()
    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    if not isinstance(manifest.get("plants"), dict) or not manifest["plants"]:
        raise SystemExit("Manifest must contain a nonempty plants object")
    report = []
    staged = []
    seen_species = set()
    seen_targets = set()
    for source_path, config in sorted(manifest["plants"].items()):
        try:
            normalized = Path(source_path)
            if normalized.is_absolute() or ".." in normalized.parts or normalized.suffix != ".json":
                raise ValueError("unsafe source file path")
            if not isinstance(config, dict):
                raise ValueError("manifest crop configuration must be an object")
            src = json.loads((args.source / normalized).read_text(encoding="utf-8"))
            plant, visuals = convert_crop(src, config)
            if plant["id"] in seen_species:
                raise ValueError(f"duplicate plant ID in manifest: {plant['id']}")
            name = normalized.stem
            targets = [
                (Path("data/cropgenetics/cropgenetics/plants") / (name + ".json"), plant),
                (Path("assets/cropgenetics/cropgenetics/plant_visuals") / (name + ".json"), visuals)
            ]
            for relative, document in targets:
                safe_target(args.output, relative)
                if relative in seen_targets:
                    raise ValueError(f"duplicate output path: {relative}")
            seen_species.add(plant["id"])
            seen_targets.update(relative for relative, _ in targets)
            staged.extend(targets)
            report.append({"source": source_path, "plant": plant["id"], "status": "ready"})
        except (KeyError, TypeError, ValueError, OSError, json.JSONDecodeError) as exc:
            report.append({"source": source_path, "status": "skipped", "reason": str(exc)})
    print(json.dumps({"ready": len(report) - sum(r["status"] == "skipped" for r in report),
                      "skipped": sum(r["status"] == "skipped" for r in report),
                      "items": report}, indent=2, ensure_ascii=False))
    if any(r["status"] == "skipped" for r in report):
        raise SystemExit("Preflight failed; no output files written")
    # Recheck immediately before writing to limit accidental overwrites.
    for relative, _ in staged:
        safe_target(args.output, relative)
    for relative, document in staged:
        output_file(args.output, relative, document)
    print(f"Wrote {len(staged)} new resource files; review Git diff before publishing")

if __name__ == "__main__":
    main()
