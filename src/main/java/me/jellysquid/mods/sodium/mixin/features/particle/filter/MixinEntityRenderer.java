package me.jellysquid.mods.sodium.mixin.features.particle.filter;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.hivens.vitrine.Vitrine;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    // Only the particle calls are wrapped: the same method also plays the rain sound.
    @WrapWithCondition(method = "addRainParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private boolean filterRainSplash(WorldClient world, EnumParticleTypes type, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int[] parameters) {
        return Vitrine.options().particles.rainSplash;
    }
}
