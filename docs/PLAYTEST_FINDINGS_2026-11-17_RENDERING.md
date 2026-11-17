# Manual Playtest 2026-11-17 – Plant rendering follow-up

Source: user-supplied `mod-test-results-2026-11-17.json`, with follow-up clarification on the same date.

## Observed (not yet verified as fixed)

- BOOT-01, BOOT-02, BOOT-03, AGRI-01, INT-01, STAB-01 and STAB-02 marked PASS.
- AGRI-02 marked BLOCKED: vanilla seed variants appear to share one item icon; crop sticks resemble parallel plates rather than four individual rods.
- AGRI-03 marked BLOCKED: first growth stage appears generic/similar for both tested species; wheat seedlings too small; fully grown wheat too large or misaligned; Identify displays only crop sticks instead of planted species and growth stage; bone meal does not grow crops; wheat appears darker than vanilla.
- Clarification: diagonal X foliage geometry occurred for **both wheat and carrots**, not only carrots.
- INT-02 marked FAIL: JEI shows only one genetic seed (wheat), not all species.

## Rendering work on integration branch

- Introduced distinct `HASH` client geometry for four-sided compact foliage instead of treating hash as diagonal `CROSS`.
- Changed wheat, carrots, potatoes, beetroots JSON visual definitions to `geometry: hash`. Explicit CROSS and GOURD species remain unchanged.
- Four HASH faces use a central 0.27–0.73 horizontal square, and the renderer bounds crop height to 0.28–0.78 blocks with stage-dependent scaling. This addresses the X arrangement and seedling visibility, but still needs in-game confirmation.

## Remaining work

- Visual stage 0 species distinction requires testing; the vanilla textures for some species have very similar early appearances.
- Four individual crop sticks rod model and differentiated seed item icons.
- Identify overlay: expose species and growth stage without exposing unanalysed genetic values.
- JEI ingredient variants for genetic seeds.
- Bone meal behavior and updated crop tint.
- Compile and playtest all changes; the above client renderer edits have not been built or tested.

## Consolidated implementation after first rendering patch (still untested)

- Crop stick and crossed stick block models now have four separate corner rods instead of plate geometry.
- Bone meal advances one actual server-side plant stage for immature planted crops; configured soil treatment remains available as a fallback for mature crops/otherwise treatable soil.
- A standalone `CropInfoOverlay` displays plant species and exact stage/total, without displaying genetic alleles. Identify 0.4.0's client-side source does not expose a supported general-purpose public overlay integration; the Crop Genetics display therefore works independently alongside Identify. Its exact HUD positioning must be tested.
- JEI: optional compile-only dependency on JEI 19.64.0.455 API; a `@JeiPlugin` class registers each built-in genetic seed species as a distinct subtype and extra ingredient without copying genetic trait values to an unanalysed UI.
- The genetic seed has species-specific item-model overrides, matching the same built-in species ordering used by the Creative inventory and JEI integration. Unsupported datapack-added species currently fall back to the wheat-seed icon.
- `-PloadTestMods` now includes all JARs from `neoforge-port/test-mods` as opt-in runtime dependencies.

## Remaining verification and limitations

- **No Gradle build or in-game test performed yet** for these changes; compile and optional-mod combinations are not certified.
- Original AgriCraft exact 1:1 visuals, precise seedling textures, and reported slightly-dark wheat coloration require side-by-side visual verification. The new hash geometry does not promise pixel-level parity.
- The optional JEI API requires resolution from BlameJared Maven on the first Gradle build. Actual JEI 19.64.0.455 integration remains unconfirmed until launch.
- Identify cannot replace its own crop-sticks title using a stable external extension API; the separate Crop Genetics HUD is a deliberate fallback.
- Pam's Crops was not installed in the first user test; do not mark it PASS.
