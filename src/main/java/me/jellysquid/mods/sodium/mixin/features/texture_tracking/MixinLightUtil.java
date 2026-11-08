package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Item models are drawn quad by quad through here, both by Forge's RenderItem.renderQuads and by mods that render
 * items themselves, such as JEI's ingredient list.
 */
@Mixin(value = LightUtil.class, remap = false)
public class MixinLightUtil {
    @Inject(method = "renderQuadColor", at = @At("HEAD"))
    private static void markQuadSprite(BufferBuilder buffer, BakedQuad quad, int auxColor, CallbackInfo ci) {
        SpriteUtil.markSpriteActive(quad.getSprite());
    }
}
