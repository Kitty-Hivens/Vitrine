package dev.hivens.vitrine.compat;

import com.google.common.collect.ImmutableList;
import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.ILateMixinLoader;
import zone.rong.mixinbooter.MixinLoader;

import java.util.List;

/**
 * Mixins into other mods' classes, which exist only once mods are loading. Each config is queued only when the
 * mod it patches is present, so a pack without that mod never sees it.
 */
@MixinLoader
public final class VitrineLateMixins implements ILateMixinLoader {
    private static final String AE2_CONFIG = "mixins.vitrine.compat.ae2.json";

    @Override
    public List<String> getMixinConfigs() {
        return ImmutableList.of(AE2_CONFIG);
    }

    @Override
    public boolean shouldMixinConfigQueue(Context context) {
        if (AE2_CONFIG.equals(context.mixinConfig())) {
            return context.isModPresent("appliedenergistics2");
        }
        return true;
    }
}
