package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureMap.class)
public class MixinTextureMap {
    // The condition wraps the call site rather than the sprite's own method, so sprite subclasses that replace
    // updateAnimation (FoamFix, custom mod sprites) are gated the same way.
    @WrapWithCondition(method = "updateAnimations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;updateAnimation()V"))
    private boolean shouldAnimate(TextureAtlasSprite sprite) {
        return SpriteUtil.shouldAnimate(sprite);
    }

    @Inject(method = "getAtlasSprite", at = @At("RETURN"))
    private void markRequested(String iconName, CallbackInfoReturnable<TextureAtlasSprite> cir) {
        SpriteUtil.markSpriteRequested(cir.getReturnValue());
    }
}
