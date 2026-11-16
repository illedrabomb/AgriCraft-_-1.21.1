# Original AgriCraft visual assets in the NeoForge port

The following original files are copied **byte-for-byte** from the repository's existing `src/main/resources/assets/agricraft/textures/` directory into `neoforge-port/src/main/resources/assets/cropgenetics/textures/`:

- `block/crop_sticks_wood.png`
- `item/crop_sticks_wood.png`
- `plant/melon1.png` through `plant/melon4.png`
- `plant/pumpkin1.png` through `plant/pumpkin4.png`

Original crop-stick block/cross models use `cropgenetics:block/crop_sticks_wood` instead of Vanilla planks. The crop-stick item model uses the original flat `cropgenetics:item/crop_sticks_wood` graphic.

The client plant visuals for melon and pumpkin use four original AgriCraft frames, distributed over the exact current growth stage count. The physical melon and pumpkin fruit blocks and their Vanilla loot delegation remain unchanged. Other crops (e.g. wheat, carrot) keep their existing Vanilla resources until a verified equivalent original texture is identified.

The `assets/minecraft/atlases/blocks.json` source already includes the `textures/plant` directory, so the new `cropgenetics:plant/...` references follow the same atlas conventions. Rendering is not yet confirmed in-game.

The original repository has an MIT license with Copyright (c) 2014 InfinityRaider. The complete original LICENSE is also copied into `neoforge-port/LICENSE`. Third-party textures from other mods are **not** copied or assumed to share that license.

**No builds, tests, CI, or Minecraft starts were performed.** Verify textures, sprite atlas registration, transparency, crop-stick item appearance, and gourd growth frames locally before release.
