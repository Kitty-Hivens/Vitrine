package org.embeddedt.embeddium.compat.fluidlogged_api;

import git.jbredwards.fluidlogged_api.api.capability.IFluidStateCapability;
import git.jbredwards.fluidlogged_api.api.capability.IFluidStateContainer;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.Chunk;

import java.util.Objects;

/**
 * Holds a 16x16x16 copy of {@link IFluidStateContainer} data.
 */
public class FluidStateStorage {
    private final BlockStateContainer data = new BlockStateContainer();

    public FluidStateStorage(Chunk chunkIn, int yIn) {
        IFluidStateContainer container = Objects.requireNonNull(IFluidStateCapability.get(chunkIn)).getContainer(yIn);
        container.forEach((pos, fluidState) -> {
            int y = container.deserializeY(pos);
            if (y >> 4 == yIn >> 4) {
                int x = container.deserializeX(pos);
                int z = container.deserializeZ(pos);

                data.set(x & 15, y & 15, z & 15, fluidState.getState());
            }
        });
    }

    public Object get(int x, int y, int z) {
        return FluidState.of(data.get(x, y, z));
    }
}