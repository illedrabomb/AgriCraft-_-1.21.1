# DESTRUCTIVE F2/F3 preparation; only use in a disposable backed-up world.
# Relative offsets use the SAME original anchor as build_f0_f1.
# No block entities, crops, seeds or test outcomes are created.

# F2: independent harvest beds for standard produce, melon and pumpkin.
fill ~120 ~0 ~0 ~145 ~3 ~8 minecraft:air
fill ~120 ~-1 ~0 ~145 ~-1 ~8 minecraft:farmland
setblock ~124 ~-1 ~8 minecraft:water
setblock ~132 ~-1 ~8 minecraft:water
setblock ~140 ~-1 ~8 minecraft:water

# F3: analyzer/seed-chest platform with floor markers only.
# Blue/analyzer (+159,+4), lime/direct chest (+160,+4).
# Red/analyzer (+169,+4), yellow/diagonal chest (+170,+5).
fill ~155 ~0 ~0 ~173 ~3 ~8 minecraft:air
fill ~155 ~-1 ~0 ~173 ~-1 ~8 minecraft:polished_andesite
setblock ~159 ~-1 ~4 minecraft:blue_concrete
setblock ~160 ~-1 ~4 minecraft:lime_concrete
setblock ~169 ~-1 ~4 minecraft:red_concrete
setblock ~170 ~-1 ~5 minecraft:yellow_concrete
