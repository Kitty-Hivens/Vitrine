package dev.hivens.vitrine.mixin.compat.ae2;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AE2 keeps the quads of every cable shape it has built in one static map, filled through computeIfAbsent from
 * getQuads. The map is a HashMap, and chunk meshes are built on several threads at once, vanilla's as much as ours:
 * two builders reaching cables together make it throw ConcurrentModificationException and fail the chunk.
 *
 * The map is replaced by a ConcurrentHashMap as soon as the class has created it, before anything can use it. Its
 * computeIfAbsent is atomic, and the function AE2 passes builds a list without touching the cache again, which is
 * the one thing ConcurrentHashMap forbids. Values are never null. The field is declared as Map, so nothing else in
 * AE2 notices.
 */
@Pseudo
@Mixin(targets = "appeng.client.render.cablebus.CableBusBakedModel", remap = false)
public abstract class MixinCableBusBakedModel {
    @Shadow
    @Final
    @Mutable
    private static Map<Object, Object> CABLE_MODEL_CACHE;

    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void vitrine$makeCableCacheConcurrent(CallbackInfo ci) {
        CABLE_MODEL_CACHE = new ConcurrentHashMap<>();
    }
}
