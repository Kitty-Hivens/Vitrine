package me.jellysquid.mods.sodium.client.world.biome;

import dev.hivens.vitrine.Vitrine;
import me.jellysquid.mods.sodium.client.util.color.ColorARGB;
import me.jellysquid.mods.sodium.client.util.math.ChunkSectionPos;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeColorHelper;

public class BiomeColorCache {
    private static final int BLENDED_COLORS_DIM = 16 + 2 * 2;

    private final BiomeColorHelper.ColorResolver resolver;
    private final WorldSlice slice;

    private final int[] blendedColors;
    private final int[] cache;
    // Separate populated flags: a color sentinel (-1) collides with packed white (0xFFFFFFFF).
    private final boolean[] blendedColorsComputed;
    private final boolean[] cacheComputed;

    private final int radius;
    private final int dim;
    private final int minX, minZ;

    private final int height;

    private final int blendedColorsMinX;
    private final int blendedColorsMinZ;

    public BiomeColorCache(BiomeColorHelper.ColorResolver resolver, WorldSlice slice) {
        this.resolver = resolver;
        this.slice = slice;
        this.radius = Vitrine.options().quality.biomeBlendRadius;

        ChunkSectionPos origin = this.slice.getOrigin();

        this.minX = origin.getMinX() - (this.radius + 2);
        this.minZ = origin.getMinZ() - (this.radius + 2);

        this.height = origin.getMinY();
        this.dim = 16 + ((this.radius + 2) * 2);

        this.blendedColorsMinX = origin.getMinX() - 2;
        this.blendedColorsMinZ = origin.getMinZ() - 2;

        this.cache = new int[this.dim * this.dim];
        this.blendedColors = new int[BLENDED_COLORS_DIM * BLENDED_COLORS_DIM];
        this.cacheComputed = new boolean[this.cache.length];
        this.blendedColorsComputed = new boolean[this.blendedColors.length];
    }

    public int getBlendedColor(BlockPos pos) {
        int x2 = pos.getX() - this.blendedColorsMinX;
        int z2 = pos.getZ() - this.blendedColorsMinZ;

        int index = (x2 * BLENDED_COLORS_DIM) + z2;

        if (!this.blendedColorsComputed[index]) {
            this.blendedColors[index] = this.calculateBlendedColor(pos.getX(), pos.getZ());
            this.blendedColorsComputed[index] = true;
        }

        return this.blendedColors[index];
    }

    private int calculateBlendedColor(int posX, int posZ) {
        if (this.radius == 0) {
            return this.getColor(posX, posZ);
        }

        int diameter = (this.radius * 2) + 1;
        int area = diameter * diameter;

        int r = 0;
        int g = 0;
        int b = 0;

        int minX = posX - this.radius;
        int minZ = posZ - this.radius;

        int maxX = posX + this.radius;
        int maxZ = posZ + this.radius;

        for (int x2 = minX; x2 <= maxX; x2++) {
            for (int z2 = minZ; z2 <= maxZ; z2++) {
                int color = this.getColor(x2, z2);

                r += ColorARGB.unpackRed(color);
                g += ColorARGB.unpackGreen(color);
                b += ColorARGB.unpackBlue(color);
            }
        }

        return ColorARGB.pack(r / area, g / area, b / area, 255);
    }

    private int getColor(int x, int z) {
        int index = ((x - this.minX) * this.dim) + (z - this.minZ);

        if (!this.cacheComputed[index]) {
            this.cache[index] = this.calculateColor(x, z);
            this.cacheComputed[index] = true;
        }

        return this.cache[index];
    }

    private int calculateColor(int x, int z) {
        return this.resolver.getColorAtPos(this.slice.getBiome(x, this.height, z), new BlockPos(x, this.height, z));
    }
}