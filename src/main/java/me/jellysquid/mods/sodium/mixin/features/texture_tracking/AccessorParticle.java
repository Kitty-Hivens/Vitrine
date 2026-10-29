package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface AccessorParticle {
    @Accessor("particleTexture")
    TextureAtlasSprite vitrine$getParticleTexture();
}
