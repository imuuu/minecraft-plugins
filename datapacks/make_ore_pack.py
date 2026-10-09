"""Builds the ImusOres data pack: vanilla ore generation scaled by FACTOR.

Reads the vanilla worldgen JSON straight out of the Paper server jar, so the
pack always matches the server version it was built from. Rerun after a
Minecraft update, or after changing FACTOR:

    python datapacks/make_ore_pack.py [path/to/paper-<ver>.jar]

Scattered ores: each placed feature's attempts per chunk are scaled (count,
rarity_filter, or an added 0/1 count where vanilla tries once per chunk).
Ore veins (the big iron/copper ribbons, noise_settings/overworld): a block is
in a vein where both ore_vein_a and ore_vein_b are within 0.08 of zero, so the
vein cross-section scales with the square of that threshold; it is multiplied
by sqrt(FACTOR).
"""
import json
import math
import shutil
import sys
import zipfile
from fractions import Fraction
from pathlib import Path

FACTOR = Fraction(1, 2)

ORES = [
    "ore_coal_upper", "ore_coal_lower",
    "ore_iron_upper", "ore_iron_middle", "ore_iron_small",
    "ore_copper", "ore_copper_large",
    "ore_gold", "ore_gold_lower", "ore_gold_extra",
    "ore_redstone", "ore_redstone_lower",
    "ore_lapis", "ore_lapis_buried",
    "ore_diamond", "ore_diamond_medium", "ore_diamond_large", "ore_diamond_buried",
    "ore_emerald",
    "ore_gold_nether", "ore_gold_deltas",
    "ore_quartz_nether", "ore_quartz_deltas",
    "ore_ancient_debris_large", "ore_debris_small",
]

ROOT = Path(__file__).resolve().parent
PACK = ROOT / "ImusOres"
DEFAULT_JAR = ROOT.parent / "run" / "versions" / "26.2" / "paper-26.2.jar"
DATA = "data/minecraft/worldgen/"


def mean(provider):
    if isinstance(provider, int):
        return Fraction(provider)
    if provider["type"] == "minecraft:uniform":
        return Fraction(provider["min_inclusive"] + provider["max_inclusive"], 2)
    raise ValueError(f"unsupported int provider {provider}")


def count_with_mean(m):
    """Constant when m is whole, otherwise floor/ceil weighted to average m."""
    if m.denominator == 1:
        return int(m)
    low = math.floor(m)
    up = m - low
    return {
        "type": "minecraft:weighted_list",
        "distribution": [
            {"data": low, "weight": up.denominator - up.numerator},
            {"data": low + 1, "weight": up.numerator},
        ],
    }


def scale_placement(placement):
    for i, mod in enumerate(placement):
        if mod["type"] == "minecraft:count":
            mod["count"] = count_with_mean(mean(mod["count"]) * FACTOR)
            return
        if mod["type"] == "minecraft:rarity_filter":
            chance = Fraction(mod["chance"]) / FACTOR
            if chance.denominator != 1:
                raise ValueError(f"rarity 1/{mod['chance']} can't be scaled by {FACTOR}")
            mod["chance"] = int(chance)
            return
    # One attempt per chunk in vanilla: make it 0 or 1 attempts.
    placement.insert(0, {"type": "minecraft:count", "count": count_with_mean(FACTOR)})


def main():
    jar = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_JAR
    with zipfile.ZipFile(jar) as z:
        read = lambda path: json.loads(z.read(path))
        paper_meta = read("data/minecraft/datapacks/paper/pack.mcmeta")["pack"]

        if PACK.exists():
            shutil.rmtree(PACK)
        out = PACK / DATA
        (out / "placed_feature").mkdir(parents=True)
        (out / "noise_settings").mkdir(parents=True)

        for ore in ORES:
            feature = read(f"{DATA}placed_feature/{ore}.json")
            scale_placement(feature["placement"])
            (out / "placed_feature" / f"{ore}.json").write_text(json.dumps(feature, indent=2) + "\n")

        overworld = read(f"{DATA}noise_settings/overworld.json")
        ridged = overworld["noise_router"]["vein_ridged"]
        assert ridged["type"] == "minecraft:add" and abs(ridged["argument1"] + 0.08) < 1e-6, ridged
        ridged["argument1"] = round(-0.08 * math.sqrt(FACTOR), 6)
        (out / "noise_settings" / "overworld.json").write_text(json.dumps(overworld, indent=2) + "\n")

    percent = round(float(FACTOR) * 100)
    mcmeta = {"pack": {
        "description": f"Ores at {percent} % of vanilla (new chunks only)",
        "min_format": paper_meta["min_format"],
        "max_format": paper_meta["max_format"],
    }}
    (PACK / "pack.mcmeta").write_text(json.dumps(mcmeta, indent=2) + "\n")
    print(f"{PACK}: {len(ORES)} ore features and ore veins at {percent} % (from {jar.name})")


if __name__ == "__main__":
    main()
