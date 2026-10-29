package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * With Forge's light pipeline enabled, out-of-chunk block models are piped through Forge's lighter instead of the
 * vanilla quad loops.
 */
@Mixin(value = ForgeBlockModelRenderer.class, remap = false)
public class MixinForgeBlockModelRenderer {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BakedQuad;pipe(Lnet/minecraftforge/client/model/pipeline/IVertexConsumer;)V"))
    private static void markPipedQuad(BakedQuad quad, IVertexConsumer consumer, Operation<Void> original) {
        SpriteUtil.markSpriteActive(quad.getSprite());
        original.call(quad, consumer);
    }
}
