# Vitrine manual test matrix

Pre-release verification run by hand. The runtime configuration space is combinatorial; this document picks a practical subset that exercises each independent code path at least once, then adds targeted regression checks for the fixes currently on `main`.

Run each configuration in a real modpack, not a vanilla instance -- most of the risk is in mod interaction. A large pack plus a resource pack that touches block and fluid textures (for example Faithful 32x) is the best stress case.

## Configuration axes

Every Vitrine option is under Video Settings -> the Vitrine options button. Options marked "renderer reload" re-mesh the world when changed, no restart needed; the rest apply live.

| Axis | Option (group) | Values | Effect on code path |
|---|---|---|---|
| Chunk backend | Use Chunk Multidraw (Advanced) | on / off | On = multidraw backend; off = oneshot backend. Also forced to oneshot when the driver lacks the feature or is blacklisted. |
| Vertex format | Use Compact Vertex Format (Advanced) | on / off | On = HFP (16-bit) writer/shader; off = SFP (32-bit float). |
| Translucency sort | Translucency Sorting (Advanced) | on / off | Enables the async translucency-sort task path. |
| Vertex writer | Allow Direct Memory Access (Advanced) | on / off | On = Unsafe writers; off = Nio writers. Must produce identical geometry. |
| Command-buffer branch | (hardware, not an option) | Intel-class / discrete | The multidraw backend has a separate `commandBuffer == null` path taken on drivers without persistent-mapped indirect buffers (Intel/mesa). |

Notes:
- Multidraw needs OpenGL 3.0+ VAOs and 4.3 (or ARB) multi-draw-indirect. If the toggle is greyed out or the game logs a fallback, this machine only runs the oneshot backend -- record that.
- On an Intel iGPU with mesa/i915 you are on the `commandBuffer == null` branch by default. Testing the other branch needs a discrete Nvidia/AMD GPU (or a VM with GPU passthrough).

## Matrix to run

Full 2^4 is 16 combinations plus the hardware axis; that is more than the payoff. Run the baseline, then flip one axis at a time, then the opposite corner. If a single-axis flip regresses, bisect from there.

| # | Multidraw | Compact (HFP) | Translucency sort | Direct memory | Purpose |
|---|---|---|---|---|---|
| 1 | on | on | on | on | Baseline (default settings) |
| 2 | off | on | on | on | Oneshot backend |
| 3 | on | off | on | on | SFP vertex format |
| 4 | on | on | off | on | No translucency sort |
| 5 | on | on | on | off | Nio writers (DMA off) |
| 6 | off | off | off | off | Opposite corner |

Per hardware: run at least the baseline on both an Intel iGPU (command-buffer-null branch) and, if available, a discrete GPU.

## Per-run functional checklist

For every configuration above, confirm:

- [ ] World loads; terrain, water, and lava all render (no missing or black chunks).
- [ ] Opaque block textures are correct (no foreign textures on blocks).
- [ ] Water and lava show the right texture and animate.
- [ ] Translucent blocks (stained glass, water surfaces) sort front-to-back correctly; no obvious flicker or wrong ordering, especially when the camera moves.
- [ ] Grass, foliage, and water biome tints look right and blend across biome borders.
- [ ] Entities, particles, text, and the GUI render.
- [ ] Placing and breaking blocks updates the chunk immediately.
- [ ] The block-breaking crack overlay shows while mining.
- [ ] Leaving and rejoining the world, repeatedly, does not hang or crash.
- [ ] No GL errors or chunk-build exceptions in the log.

## Regression checks for the fixes on `main`

These target specific fixes; run them at least once on the baseline configuration, and the fluid ones on both HFP and SFP.

- [ ] **Black/wrong water on in-world pack swap.** Enter a world, then toggle a resource pack that changes water/block textures. Water and blocks stay correct (this was the headline bug: NaN flow vector landing on the atlas corner, plus overlay selection).
- [ ] **Waterfalls.** A tall falling-water column renders with correct flowing texture, not a garbled or corner-atlas texture.
- [ ] **Water next to glass and leaves.** The overlay vs flow texture is chosen by block type; check water touching stained glass, plain glass, ice, and leaves.
- [ ] **Modded fluids.** Any mod fluid without a registered biome color renders in its own color, not white.
- [ ] **Fluidlogged / waterlogged blocks** (if a fluidlogged mod is present) render water correctly and occlude neighboring faces sanely.
- [ ] **Caves are dark.** A fully enclosed underground cavern (a section with no blocks in it) is not lit as if under open sky. Check the Nether too.
- [ ] **Empty/broken config.** Truncate Vitrine's options config JSON under `config/` to an empty file and launch; the game starts on defaults instead of crashing on a null parse.
- [ ] **World unload under load.** Fly fast through fresh terrain to keep the chunk builders busy, then quit to menu; no hang.
- [ ] **Rapid translucent edits.** Place and break stained glass repeatedly while looking at it; no stale/flickering translucency (the sort-vs-rebuild race).
- [ ] **Entity smooth lighting** follows the Smooth Lighting option (entities darken/smooth with the setting, not with the vanilla AO slider).
- [ ] **CTM / connected-textures mod present.** With a connected-textures mod installed, fly around near chunk borders; no `ArrayIndexOutOfBounds` from biome lookups on the chunk-build workers.

## Recording results

For each configuration, note: pass/fail per checklist item, GPU + driver, and any log lines. A failure that only appears on one axis flip localizes the regression to that path (backend, format, sort, or writer).
