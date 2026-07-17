package me.jellysquid.mods.sodium.mixin.features.entity.smooth_lighting;

import dev.hivens.vitrine.Vitrine;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.model.light.EntityLighter;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public abstract class MixinEntityRenderer<T extends Entity> {

    @Unique
    private float tickDelta;

    @Inject(method = "renderEntityStatic", at = @At("HEAD"))
    public void catchTickDelta(Entity entity, float tickDelta, boolean bl, CallbackInfo ci) {
        this.tickDelta = tickDelta;
    }

    @Redirect(method = "renderEntityStatic", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getBrightnessForRender()I"))
    private int sodium$getBrightnessForRender(Entity self) {
        if (Vitrine.options().quality.smoothLighting == SodiumGameOptions.LightingQuality.HIGH) {
            return EntityLighter.getBlendedLight(self, tickDelta);
        }

        return self.getBrightnessForRender();
    }
}