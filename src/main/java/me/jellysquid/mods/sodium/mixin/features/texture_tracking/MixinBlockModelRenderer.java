package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;

/**
 * Chunks are meshed by our own renderer, so the vanilla block renderer only draws blocks outside of chunks: falling
 * blocks, moving pistons, blocks held by entities and block models drawn by tile entity renderers.
 */
@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    @Inject(method = "renderQuadsSmooth", at = @At("HEAD"))
    private void markSmoothQuads(IBlockAccess world, IBlockState state, BlockPos pos, BufferBuilder buffer, List<BakedQuad> quads, float[] quadBounds, BitSet bitSet, @Coerce Object aoFace, CallbackInfo ci) {
        markQuadSprites(quads);
    }

    @Inject(method = "renderQuadsFlat", at = @At("HEAD"))
    private void markFlatQuads(IBlockAccess world, IBlockState state, BlockPos pos, int brightness, boolean ownBrightness, BufferBuilder buffer, List<BakedQuad> quads, BitSet bitSet, CallbackInfo ci) {
        markQuadSprites(quads);
    }

    @Inject(method = "renderModelBrightnessColorQuads", at = @At("HEAD"))
    private void markColorQuads(float brightness, float red, float green, float blue, List<BakedQuad> quads, CallbackInfo ci) {
        markQuadSprites(quads);
    }

    private static void markQuadSprites(List<BakedQuad> quads) {
        for (int i = 0, size = quads.size(); i < size; i++) {
            SpriteUtil.markSpriteActive(quads.get(i).getSprite());
        }
    }
}
