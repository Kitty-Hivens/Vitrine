package me.jellysquid.mods.sodium.client.render.chunk.compile;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderContainer;

/**
 * A chunk rebuild that threw, together with the section it was building.
 *
 * The worker that ran the build cannot touch the section's state, so the
 * failure travels to the main thread like any other result, and the main
 * thread decides whether to schedule the section again or to crash with the
 * report the build already filled in (the cause).
 */
public final class ChunkBuildFailure extends RuntimeException {
    private final ChunkRenderContainer<?> render;

    public ChunkBuildFailure(ChunkRenderContainer<?> render, Throwable cause) {
        super("Chunk build failed for " + render, cause, false, false);
        this.render = render;
    }

    public ChunkRenderContainer<?> getRender() {
        return this.render;
    }
}
