package org.embeddedt.embeddium.compat.fluidlogged_api;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import git.jbredwards.fluidlogged_api.mod.asm.plugins.vanilla.world.PluginWorld;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.MathUtil;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;

/**
 * Contains methods used for Fluidlogged API compatibility.
 */
public class FluidloggedCompat {
    public static final String MODID = "fluidlogged_api";
    public static final boolean IS_LOADED = Loader.isModLoaded(MODID);

    public static Object getEmptyFluidState() {
        return FluidState.EMPTY;
    }

    public static IBlockState getFluidOrReal(IBlockAccess access, BlockPos pos) {
        return FluidloggedUtils.getFluidOrReal(access, pos);
    }

    public static IBlockState getFluidStateRelative(WorldSlice slice, int relX, int relY, int relZ) {
        return slice.getFluidStateRelative(relX, relY, relZ).getState();
    }

    public static int getStrongPower(IBlockAccess access, BlockPos pos, EnumFacing direction) {
        // NOTE: This method may cause crashes when using a version of Fluidlogged API older than v3.0.5
        return PluginWorld.Hooks.getStrongPower(access, pos, direction);
    }

    public static boolean isCompatibleFluid(Fluid fluid1, Fluid fluid2) {
        return FluidloggedUtils.isCompatibleFluid(fluid1, fluid2);
    }

    public static boolean renderFluidState(WorldSlice slice, int relX, int relY, int relZ, BlockPos pos, IBlockState state, ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers) {
        boolean render = false;

        FluidState fluidState = slice.getFluidStateRelative(relX, relY, relZ);
        if (fluidState != FluidState.EMPTY && (!(state.getBlock() instanceof IFluidloggable) || ((IFluidloggable)state.getBlock()).shouldFluidRender(cache.getLocalSlice(), pos, state, fluidState))) {
            IBlockAccess access = cache.getLocalSlice();
            IBlockState renderState = fluidState.getState().getActualState(access, pos);

            // renders the fluid in each layer
            EnumBlockRenderType renderType = renderState.getRenderType();
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                if (!renderState.getBlock().canRenderInLayer(renderState, layer)) continue;

                ForgeHooksClient.setRenderLayer(layer);
                if (renderType == EnumBlockRenderType.MODEL) render |= cache.getBlockRenderer().renderModel(
                        access, renderState.getBlock().getExtendedState(renderState, access, pos), pos,
                        cache.getBlockModels().getModelForState(renderState), buffers.get(layer), true,
                        MathUtil.hashPos(pos));

                else if (renderType == EnumBlockRenderType.LIQUID) render |= cache.getFluidRenderer().render(
                        access, renderState, pos, buffers.get(layer));
            }

            // reset current render layer
            ForgeHooksClient.setRenderLayer(null);
        }

        return render;
    }
}
