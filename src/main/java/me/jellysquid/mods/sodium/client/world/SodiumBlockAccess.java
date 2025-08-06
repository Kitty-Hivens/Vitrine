package me.jellysquid.mods.sodium.client.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import org.embeddedt.embeddium.compat.fluidlogged_api.FluidloggedBlockAccess;

/**
 * Contains extensions to the vanilla {@link IBlockAccess}.
 */
public interface SodiumBlockAccess extends IBlockAccess, FluidloggedBlockAccess {
    int getBlockTint(BlockPos pos, BiomeColorHelper.ColorResolver resolver);
}
