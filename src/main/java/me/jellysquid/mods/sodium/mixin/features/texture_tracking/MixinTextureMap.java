package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureMap.class)
public class MixinTextureMap {
    @Unique
    private boolean vitrine$onlyVisible;

    // Only the blocks atlas animates on use. An atlas a mod creates for itself is drawn by that mod alone, often from
    // geometry compiled once, so its sprites keep animating every tick.
    @Inject(method = "updateAnimations", at = @At("HEAD"))
    private void startAnimationTick(CallbackInfo ci) {
        this.vitrine$onlyVisible = (Object) this == Minecraft.getMinecraft().getTextureMapBlocks();

        if (this.vitrine$onlyVisible) {
            SpriteUtil.onAnimationTick();
        }
    }

    // The condition wraps the call site rather than the sprite's own method, so sprite subclasses that replace
    // updateAnimation (FoamFix, custom mod sprites) are gated the same way.
    @WrapWithCondition(method = "updateAnimations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;updateAnimation()V"))
    private boolean shouldAnimate(TextureAtlasSprite sprite) {
        return SpriteUtil.shouldAnimate(sprite, this.vitrine$onlyVisible);
    }

    // Renderers that look a sprite up by name every frame, such as the fire drawn on burning entities, draw it outside
    // the tracked paths, so a lookup counts as use.
    @ModifyReturnValue(method = "getAtlasSprite", at = @At("RETURN"))
    private TextureAtlasSprite markRequested(TextureAtlasSprite sprite) {
        SpriteUtil.markSpriteRequested(sprite);

        return sprite;
    }
}
