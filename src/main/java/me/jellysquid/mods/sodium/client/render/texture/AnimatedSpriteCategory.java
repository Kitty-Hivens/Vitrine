package me.jellysquid.mods.sodium.client.render.texture;

public enum AnimatedSpriteCategory {
    WATER,
    LAVA,
    FIRE,
    PORTAL,
    TERRAIN,
    TEXTURES;

    public static AnimatedSpriteCategory of(String iconName) {
        switch (iconName) {
            case "minecraft:blocks/water_still":
            case "minecraft:blocks/water_flow":
                return WATER;
            case "minecraft:blocks/lava_still":
            case "minecraft:blocks/lava_flow":
                return LAVA;
            case "minecraft:blocks/fire_layer_0":
            case "minecraft:blocks/fire_layer_1":
                return FIRE;
            case "minecraft:blocks/portal":
                return PORTAL;
            default:
                return isBlockTexture(iconName) ? TERRAIN : TEXTURES;
        }
    }

    /**
     * Block textures live under blocks/ by convention, which vanilla and nearly every mod follow. Anything else is an
     * item, interface or mod-drawn texture.
     */
    private static boolean isBlockTexture(String iconName) {
        String path = iconName.substring(iconName.indexOf(':') + 1);

        return path.startsWith("blocks/") || path.startsWith("block/");
    }
}
