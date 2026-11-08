package me.jellysquid.mods.sodium.mixin.core.texture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.jellysquid.mods.sodium.client.render.texture.AnimatedSpriteCategory;
import me.jellysquid.mods.sodium.client.render.texture.SpriteExtended;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextureAtlasSprite.class)
public abstract class MixinTextureAtlasSprite implements SpriteExtended {
    @Shadow
    public abstract String getIconName();

    // Chunk build threads write this through texture lookups and UV reads without synchronisation. A stale read only
    // delays an animation by a tick.
    @Unique
    private int vitrine$lastActiveFrame;

    @Unique
    private AnimatedSpriteCategory vitrine$category;

    @Override
    public void vitrine$markActive(int frame) {
        // The UV getters land here once per vertex from several threads. Skipping the redundant store keeps the
        // sprite's cache line shared between them.
        if (this.vitrine$lastActiveFrame != frame) {
            this.vitrine$lastActiveFrame = frame;
        }
    }

    @Override
    public int vitrine$getLastActiveFrame() {
        return this.vitrine$lastActiveFrame;
    }

    @Override
    public AnimatedSpriteCategory vitrine$getCategory() {
        AnimatedSpriteCategory category = this.vitrine$category;

        if (category == null) {
            this.vitrine$category = category = AnimatedSpriteCategory.of(this.getIconName());
        }

        return category;
    }

    // A renderer that keeps a sprite in a field and writes its own vertices reads the sprite's UVs every time it draws,
    // so a read counts as use.
    @ModifyReturnValue(method = {"getMinU", "getMaxU", "getMinV", "getMaxV", "getInterpolatedU", "getInterpolatedV"}, at = @At("RETURN"))
    private float vitrine$markUvRead(float uv) {
        SpriteUtil.markSpriteActive((TextureAtlasSprite) (Object) this);

        return uv;
    }
}
