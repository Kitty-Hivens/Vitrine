package me.jellysquid.mods.sodium.mixin.features.particle.filter;

import dev.hivens.vitrine.Vitrine;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.client.particle.ParticleItemPickup;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    // Every particle passes through here, modded ones included, so the master switch lives here.
    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    private void filterEffect(Particle effect, CallbackInfo ci) {
        SodiumGameOptions.ParticleSettings particles = Vitrine.options().particles;

        if (!particles.enabled || !particles.isTypeEnabled(directlyAddedType(effect))) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnEffectParticle", at = @At("HEAD"), cancellable = true)
    private void filterType(int particleId, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int[] parameters, CallbackInfoReturnable<Particle> cir) {
        if (!Vitrine.options().particles.isTypeEnabled(particleId)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "addBlockDestroyEffects", at = @At("HEAD"), cancellable = true)
    private void filterBlockBreak(BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (!Vitrine.options().particles.blockBreak) {
            ci.cancel();
        }
    }

    @Inject(method = "addBlockHitEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V", at = @At("HEAD"), cancellable = true)
    private void filterBlockHit(BlockPos pos, EnumFacing side, CallbackInfo ci) {
        if (!Vitrine.options().particles.blockBreaking) {
            ci.cancel();
        }
    }

    // Forge's overload lets blocks draw their own hit particles before falling back to the vanilla ones.
    @Inject(method = "addBlockHitEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/RayTraceResult;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void filterModdedBlockHit(BlockPos pos, RayTraceResult target, CallbackInfo ci) {
        if (!Vitrine.options().particles.blockBreaking) {
            ci.cancel();
        }
    }

    /**
     * Vanilla adds fireworks and item pickups straight to the manager instead of spawning them by type id, so their
     * type is recovered from the class.
     */
    private static EnumParticleTypes directlyAddedType(Particle effect) {
        if (effect instanceof ParticleFirework.Spark || effect instanceof ParticleFirework.Starter || effect instanceof ParticleFirework.Overlay) {
            return EnumParticleTypes.FIREWORKS_SPARK;
        }

        if (effect instanceof ParticleItemPickup) {
            return EnumParticleTypes.ITEM_TAKE;
        }

        return null;
    }
}
