# Vitrine

Vitrine is a Minecraft 1.12.2 rendering-optimization mod for Forge. It replaces the vanilla chunk renderer to improve frame rate, and is an unofficial fork of [Relictium](https://github.com/TheMade4/Relictium) -- itself a fork of [Vintagium (Asek3)](https://github.com/Asek3/sodium-1.12), a Forge port of [CaffeineMC's Sodium](https://github.com/CaffeineMC/sodium).

## Status

Early fork. The baseline is functionally equivalent to Relictium 1.2.0; everything on top of it is tracked in the commit history and CHANGELOG.

## Known incompatibilities

Inherited from the Sodium/Relictium lineage:

- OptiFine -- fundamentally incompatible; use one or the other.
- Mods that also rewrite the renderer or ASM-patch the same vanilla classes: LittleTiles, FarPlaneTwo, ArchitectureCraft, Fluidlogged API (and dependents such as Subaquatic).
- Connected-textures mods (ConnectedTexturesMod / Chisel) can mis-render terrain after an in-world resource reload (toggling a pack while a world is open). Apply resource packs at the main menu before entering a world; restart to change them.

## Building

A JDK is required; the Gradle toolchain provisions Java 8 automatically.

    ./gradlew build

The jar is written to `build/libs/`.

## License

Vitrine is licensed under the LGPL-3.0, the same as its upstreams. See [LICENSE](LICENSE).

Lineage and attribution: Sodium (c) CaffeineMC; Vintagium (c) Asek3; Relictium (c) TheMade4. Vitrine continues that line under the same license.
