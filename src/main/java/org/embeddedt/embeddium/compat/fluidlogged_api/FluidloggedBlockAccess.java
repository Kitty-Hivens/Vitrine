package org.embeddedt.embeddium.compat.fluidlogged_api;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.world.IFluidStateProvider;
import git.jbredwards.fluidlogged_api.api.world.IWorldProvider;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

/**
 * Allows Fluidlogged API to make use of Sodium's optimizations.
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = FluidloggedCompat.MODID, iface = "git.jbredwards.fluidlogged_api.api.world.IFluidStateProvider"),
        @Optional.Interface(modid = FluidloggedCompat.MODID, iface = "git.jbredwards.fluidlogged_api.api.world.IWorldProvider")
})
public interface FluidloggedBlockAccess extends IBlockAccess, IFluidStateProvider, IWorldProvider {
    @Override
    @Optional.Method(modid = FluidloggedCompat.MODID)
    FluidState getFluidState(int x, int y, int z);

    @Override
    @Optional.Method(modid = FluidloggedCompat.MODID)
    World getWorld();
}