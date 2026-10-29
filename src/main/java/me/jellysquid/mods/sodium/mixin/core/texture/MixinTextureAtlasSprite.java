package me.jellysquid.mods.sodium.mixin.core.texture;

import me.jellysquid.mods.sodium.client.render.texture.AnimatedSpriteCategory;
import me.jellysquid.mods.sodium.client.render.texture.SpriteExtended;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureAtlasSprite.class)
public abstract class MixinTextureAtlasSprite implements SpriteExtended {
    @Shadow
    public abstract String getIconName();

    // Chunk build threads write these through texture lookups without synchronisation. A stale read only delays an
    // animation by a tick.
    @Unique
    private boolean vitrine$tracked;

    @Unique
    private int vitrine$lastActiveFrame;

    @Unique
    private AnimatedSpriteCategory vitrine$category;

    @Override
    public void vitrine$markActive(int frame, boolean rendered) {
        this.vitrine$lastActiveFrame = frame;

        if (rendered) {
            this.vitrine$tracked = true;
        }
    }

    @Override
    public boolean vitrine$isTracked() {
        return this.vitrine$tracked;
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
}
