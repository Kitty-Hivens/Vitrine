package me.jellysquid.mods.sodium.client.render.texture;

import dev.hivens.vitrine.Vitrine;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteUtil {
    private static int frame;

    public static void onFrameStart() {
        frame++;
    }

    /**
     * Marks a sprite drawn by a tracked render path. From then on the sprite only animates while something draws it.
     */
    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (sprite != null) {
            ((SpriteExtended) sprite).vitrine$markActive(frame, true);
        }
    }

    /**
     * Marks a sprite looked up by name. Whoever looked it up may draw it outside the tracked render paths, so this
     * keeps the sprite animating without opting it into on-demand animation.
     */
    public static void markSpriteRequested(TextureAtlasSprite sprite) {
        if (sprite != null) {
            ((SpriteExtended) sprite).vitrine$markActive(frame, false);
        }
    }

    public static boolean shouldAnimate(TextureAtlasSprite sprite) {
        SodiumGameOptions options = Vitrine.options();
        SpriteExtended extended = (SpriteExtended) sprite;

        if (!options.animations.isEnabled(extended.vitrine$getCategory())) {
            return false;
        }

        // A sprite no tracked path has drawn may be drawn by something we cannot see, such as a custom renderer
        // holding on to the sprite, so it keeps animating unconditionally.
        if (!options.advanced.animateOnlyVisibleTextures || !extended.vitrine$isTracked()) {
            return true;
        }

        // Ticks run between frames, so a sprite drawn in the latest frame differs by zero. One frame of slack keeps
        // a sprite that misses a single frame, such as a particle at the edge of the view, from stalling.
        return frame - extended.vitrine$getLastActiveFrame() <= 1;
    }
}
