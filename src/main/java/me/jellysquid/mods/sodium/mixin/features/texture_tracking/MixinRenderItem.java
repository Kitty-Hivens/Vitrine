package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @Inject(method = "renderQuads", at = @At("HEAD"))
    private void markQuadSprites(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack, CallbackInfo ci) {
        for (int i = 0, size = quads.size(); i < size; i++) {
            SpriteUtil.markSpriteActive(quads.get(i).getSprite());
        }
    }
}
