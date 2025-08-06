package me.jellysquid.mods.sodium.common.util;

import me.jellysquid.mods.sodium.client.world.VanillaFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import org.embeddedt.embeddium.compat.fluidlogged_api.FluidloggedCompat;
import repack.joml.Vector3d;

/**
 * Contains methods stripped from BlockState or FluidState that didn't actually need to be there. Technically these
 * could be a mixin to Block or Fluid, but that's annoying while not actually providing any benefit.
 */
public class WorldUtil {

    public static Vector3d getVelocity(IBlockAccess world, BlockPos pos, IBlockState thizz) {
        Vector3d velocity = new Vector3d();
        int decay = getEffectiveFlowDecay(world, pos, thizz);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos);

        for (EnumFacing dire : DirectionUtil.HORIZONTAL_DIRECTIONS) {
            int adjX = pos.getX() + dire.getXOffset();
            int adjZ = pos.getZ() + dire.getZOffset();
            mutable.setPos(adjX, pos.getY(), adjZ);

            int adjDecay = getEffectiveFlowDecay(world, mutable, thizz);

            if (adjDecay < 0) {
                if (!world.getBlockState(mutable).getMaterial().blocksMovement()) {
                    adjDecay = getEffectiveFlowDecay(world, mutable.down(), thizz);

                    if (adjDecay >= 0) {
                        adjDecay -= (decay - 8);
                        velocity = velocity.add((adjX - pos.getX()) * adjDecay, 0, (adjZ - pos.getZ()) * adjDecay);
                    }
                }
            } else {
                adjDecay -= decay;
                velocity = velocity.add((adjX - pos.getX()) * adjDecay, 0, (adjZ - pos.getZ()) * adjDecay);
            }
        }

        IBlockState state = WorldUtil.getFluidFromWorld(world, pos);
        if (state.getValue(BlockLiquid.LEVEL) >= 8) {
            if (thizz.isSideSolid(world, pos.north(), EnumFacing.NORTH)
                    || thizz.isSideSolid(world, pos.south(), EnumFacing.SOUTH)
                    || thizz.isSideSolid(world, pos.west(), EnumFacing.WEST)
                    || thizz.isSideSolid(world, pos.east(), EnumFacing.EAST)
                    || thizz.isSideSolid(world, pos.up().south(), EnumFacing.NORTH)
                    || thizz.isSideSolid(world, pos.up().west(), EnumFacing.SOUTH)
                    || thizz.isSideSolid(world, pos.up().west(), EnumFacing.WEST)
                    || thizz.isSideSolid(world, pos.up().east(), EnumFacing.EAST)) {
                velocity = velocity.normalize().add(0.0D, -6.0D, 0.0D);
            }
        }

        if (velocity.x == 0 && velocity.y == 0 && velocity.z == 0)
            return velocity.zero();
        return velocity.normalize();
    }

    /**
     * Returns true if any block in a 3x1x3 area is not the same fluid and not a full block.
     * Equivalent to BlockLiquid::shouldRenderSides, or FluidState::method_15756 in modern.
     */
    public static boolean shouldRenderSides(IBlockAccess world, BlockPos pos, Fluid fluid) {
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                IBlockState block = getFluidFromWorld(world, pos.add(i, 0, j));
                if (!block.isFullBlock() && !isSame(getFluid(block), fluid)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns fluid height as a percentage of the block; 0 is none and 1 is full.
     */
    public static float getFluidHeight(Fluid fluid, int meta) {
        return fluid == null ? 0 : 1 - BlockLiquid.getLiquidHeightPercent(meta);
    }

    /**
     * Returns the flow decay but converts values indicating falling liquid (values >=8) to their effective source block
     * value of zero
     */
    public static int getEffectiveFlowDecay(IBlockAccess world, BlockPos pos, IBlockState thiz) {
        IBlockState state = getFluidFromWorld(world, pos);
        if (!isSame(getFluid(thiz), getFluid(state))) {
            return -1;
        } else {
            int decay = state.getValue(BlockLiquid.LEVEL);
            return decay >= 8 ? 0 : decay;
        }
    }

    // I believe forge mappings in modern say BreakableBlock, while yarn says TransparentBlock.
    // I have a sneaking suspicion isOpaque is neither, but it works for now
    public static boolean shouldDisplayFluidOverlay(IBlockState block) {
        return !block.getMaterial().isOpaque() || block.getMaterial() == Material.LEAVES;
    }

    public static Fluid getFluid(IBlockState b) {
        IFluidBlock fluidBlock = toFluidBlock(b.getBlock());
        if (fluidBlock != null) return fluidBlock.getFluid();

        Material m = b.getMaterial();
        return m == Material.WATER ? FluidRegistry.WATER : m == Material.LAVA ? FluidRegistry.LAVA : null;
    }

    public static IBlockState getFluidFromWorld(IBlockAccess world, BlockPos pos) {
        return FluidloggedCompat.IS_LOADED ? FluidloggedCompat.getFluidOrReal(world, pos) : world.getBlockState(pos);
    }

    public static boolean isSame(Fluid fluid, Fluid otherFluid) {
        return FluidloggedCompat.IS_LOADED ? FluidloggedCompat.isCompatibleFluid(fluid, otherFluid) : fluid == otherFluid;
    }

    /**
     * Equivalent to method_15748 in 1.16.5
     */
    public static boolean isEmptyOrSame(Fluid fluid, Fluid otherFluid) {
        return otherFluid == null || isSame(fluid, otherFluid);
    }

    /**
     * Equivalent to method_15749 in 1.16.5
     */
    public static boolean method_15749(IBlockAccess world, Fluid thiz, BlockPos pos, EnumFacing dir) {
        IBlockState b = getFluidFromWorld(world, pos);
        Fluid f = getFluid(b);
        if (isSame(f, thiz)) {
            return false;
        }
        if (dir == EnumFacing.UP) {
            return true;
        }
        return b.getMaterial() != Material.ICE && b.isSideSolid(world, pos, dir);
    }

    public static IFluidBlock toFluidBlock(Block block) {
        if(block instanceof IFluidBlock) {
            return (IFluidBlock) block;
        } else if(block instanceof VanillaFluidBlock) {
            return ((VanillaFluidBlock) block).getFakeFluidBlock();
        } else {
            return null;
        }
    }

    @Deprecated // Use state-sensitive method instead.
    public static Fluid getFluidOfBlock(Block block) {
        return getFluid(block.getDefaultState());
    }
}