package me.jellysquid.mods.sodium.client.render.texture;

public interface SpriteExtended {
    /**
     * Records that the sprite was used during the given frame.
     */
    void vitrine$markActive(int frame);

    int vitrine$getLastActiveFrame();

    AnimatedSpriteCategory vitrine$getCategory();
}
