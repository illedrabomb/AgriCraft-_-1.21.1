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
