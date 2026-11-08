package me.jellysquid.mods.sodium.client.render.texture;

import dev.hivens.vitrine.Vitrine;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildThread;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteUtil {
    private static int frame;

    private static int frameAtLastTick;

    // Sprites last used in this frame or later animate on the current tick.
    private static int activeSince;

    public static void onFrameStart() {
        frame++;
    }

    /**
     * Opens an animation tick of the blocks atlas. At high frame rates several frames pass between two ticks and a
     * sprite used in any of them counts. At low frame rates several ticks share one frame, and the frame before it
     * still counts, so a sprite that misses a single frame, such as a particle at the edge of the view, keeps going.
     */
    public static void onAnimationTick() {
        int previousFrame = frame - 1;

        activeSince = previousFrame - frameAtLastTick > 0 ? frameAtLastTick : previousFrame;
        frameAtLastTick = frame;
    }

    /**
     * Marks a sprite drawn in the current frame. On a chunk build worker that is collecting sprites, the sprite is also
     * added to the chunk being built.
     */
    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (sprite == null) {
            return;
        }

        ((SpriteExtended) sprite).vitrine$markActive(frame);

        Thread thread = Thread.currentThread();

        if (thread instanceof ChunkBuildThread) {
            ((ChunkBuildThread) thread).collectSprite(sprite);
        }
    }

    /**
     * Marks a sprite looked up by name. Chunk build workers look up sprites they may never draw, so a lookup is not
     * collected into the chunk.
     */
    public static void markSpriteRequested(TextureAtlasSprite sprite) {
        if (sprite != null) {
            ((SpriteExtended) sprite).vitrine$markActive(frame);
        }
    }

    /**
     * @param onlyVisible True for an atlas whose sprites animate only while used
     */
    public static boolean shouldAnimate(TextureAtlasSprite sprite, boolean onlyVisible) {
        SodiumGameOptions options = Vitrine.options();
        SpriteExtended extended = (SpriteExtended) sprite;

        if (!options.animations.isEnabled(extended.vitrine$getCategory())) {
            return false;
        }

        if (!onlyVisible || !options.advanced.animateOnlyVisibleTextures) {
            return true;
        }

        return extended.vitrine$getLastActiveFrame() - activeSince >= 0;
    }
}
