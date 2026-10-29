package me.jellysquid.mods.sodium.client.render.texture;

public interface SpriteExtended {
    /**
     * Records that the sprite was used during the given frame.
     * @param rendered True if a tracked render path drew the sprite, which opts it into on-demand animation
     */
    void vitrine$markActive(int frame, boolean rendered);

    /**
     * @return True once a tracked render path has drawn this sprite
     */
    boolean vitrine$isTracked();

    int vitrine$getLastActiveFrame();

    AnimatedSpriteCategory vitrine$getCategory();
}
