# DESTRUCTIVE TERRAIN PREPARATION: new disposable test world only.
# Anchor: execution position at the future WALKING surface; plots are east (+X).
# No crops, genes, maturity, gamerules, tests or result statuses are touched.

# F0: two ordinary seed plots plus space for incorrect-soil comparisons.
fill ~0 ~0 ~0 ~12 ~3 ~8 minecraft:air
fill ~0 ~-1 ~0 ~12 ~-1 ~8 minecraft:farmland
setblock ~3 ~-1 ~7 minecraft:water
setblock ~9 ~-1 ~7 minecraft:water

# F1-A: one direct parent; independent nine-by-nine beds (center +24,+4).
fill ~20 ~0 ~0 ~28 ~3 ~8 minecraft:air
fill ~20 ~-1 ~0 ~28 ~-1 ~8 minecraft:farmland
setblock ~24 ~-1 ~8 minecraft:water

# F1-B: two direct parents (center +40,+4).
fill ~36 ~0 ~0 ~44 ~3 ~8 minecraft:air
fill ~36 ~-1 ~0 ~44 ~-1 ~8 minecraft:farmland
setblock ~40 ~-1 ~8 minecraft:water

# F1-C: three direct parents (center +56,+4).
fill ~52 ~0 ~0 ~60 ~3 ~8 minecraft:air
fill ~52 ~-1 ~0 ~60 ~-1 ~8 minecraft:farmland
setblock ~56 ~-1 ~8 minecraft:water

# F1-D: four direct parents (center +72,+4).
fill ~68 ~0 ~0 ~76 ~3 ~8 minecraft:air
fill ~68 ~-1 ~0 ~76 ~-1 ~8 minecraft:farmland
setblock ~72 ~-1 ~8 minecraft:water

# F1-E: row bed, empty target at +88,+4; parents extend east +89..+108.
# One directly adjacent parent only: do not put parents north/south of target.
fill ~84 ~0 ~0 ~110 ~3 ~8 minecraft:air
fill ~84 ~-1 ~0 ~110 ~-1 ~8 minecraft:farmland
setblock ~88 ~-1 ~8 minecraft:water
setblock ~96 ~-1 ~8 minecraft:water
setblock ~104 ~-1 ~8 minecraft:water
setblock ~108 ~-1 ~8 minecraft:water

tellraw @s {"text":"AgriCraft test terrain F0/F1 prepared; crops and tests NOT executed.","color":"yellow"}
