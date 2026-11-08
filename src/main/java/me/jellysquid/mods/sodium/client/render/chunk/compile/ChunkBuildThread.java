package me.jellysquid.mods.sodium.client.render.chunk.compile;

import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * A chunk build worker. While it runs a block renderer that writes vertices without telling us which sprites they
 * use, every sprite that renderer marks as used is collected into the chunk, so its animation runs while the chunk is
 * visible.
 */
public class ChunkBuildThread extends Thread {
    private ChunkRenderData.Builder spriteSink;

    public ChunkBuildThread(Runnable target, String name) {
        super(target, name);
    }

    /**
     * Starts collecting sprites on the current thread into the given chunk. Does nothing outside a chunk build worker.
     */
    public static void beginCollectingSprites(ChunkRenderData.Builder sink) {
        Thread thread = Thread.currentThread();

        if (thread instanceof ChunkBuildThread) {
            ((ChunkBuildThread) thread).spriteSink = sink;
        }
    }

    public static void endCollectingSprites() {
        Thread thread = Thread.currentThread();

        if (thread instanceof ChunkBuildThread) {
            ((ChunkBuildThread) thread).spriteSink = null;
        }
    }

    public void collectSprite(TextureAtlasSprite sprite) {
        ChunkRenderData.Builder sink = this.spriteSink;

        if (sink != null) {
            sink.addSprite(sprite);
        }
    }
}
