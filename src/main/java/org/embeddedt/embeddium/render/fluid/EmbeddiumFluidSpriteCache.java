package org.embeddedt.embeddium.render.fluid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * The still, flowing and overlay sprites of a fluid, as the block atlas holds
 * them now.
 *
 * No sprite is remembered between calls, and that is the whole point. A
 * resource reload restitches the atlas and replaces every sprite object in it,
 * and a remembered one keeps the texture coordinates it had in the atlas
 * before: a rectangle that now belongs to some other texture entirely. Every
 * fluid meshed afterwards is then drawn with whatever moved into that
 * rectangle, which is the long standing fault where water shows another block
 * after a resource pack is switched in the world.
 *
 * Reading the vanilla renderer's own fields does not avoid it. Those are
 * refreshed by a reload listener, and on a pack change that refresh does not
 * reliably happen before the chunk renderer rebuilds, so they go stale the same
 * way. Vanilla never notices, because once this renderer takes over, nothing
 * reads them.
 *
 * Fluids are the only blocks this can reach. Everything else is drawn from a
 * baked model, and models are baked again on reload, so their coordinates are
 * always current.
 *
 * The cost is one hash lookup per sprite, against the neighbour reads, corner
 * heights and lighting the caller already does for the same block. It is not a
 * hot path worth a stale snapshot.
 */
public class EmbeddiumFluidSpriteCache {

    /** The names vanilla's own fluid renderer looks up, so the result matches it. */
    private static final String WATER_STILL = "minecraft:blocks/water_still";
    private static final String WATER_FLOW = "minecraft:blocks/water_flow";
    private static final String WATER_OVERLAY = "minecraft:blocks/water_overlay";
    private static final String LAVA_STILL = "minecraft:blocks/lava_still";
    private static final String LAVA_FLOW = "minecraft:blocks/lava_flow";

    /** Handed back to the caller, so a mesh build allocates nothing per fluid block. */
    private final TextureAtlasSprite[] sprites = new TextureAtlasSprite[3];

    /**
     * Only the name is worth keeping. A sprite goes stale, a name does not, and
     * {@link ResourceLocation#toString()} builds a new string on every call.
     */
    private final Object2ObjectOpenHashMap<ResourceLocation, String> names =
        new Object2ObjectOpenHashMap<ResourceLocation, String>();

    private static TextureAtlasSprite sprite(String name) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
    }

    private TextureAtlasSprite getTexture(ResourceLocation identifier) {
        String name = names.get(identifier);

        if (name == null) {
            name = identifier.toString();
            names.put(identifier, name);
        }

        return sprite(name);
    }

    public TextureAtlasSprite[] getSprites(Fluid fluid) {
        if (fluid == FluidRegistry.WATER) {
            sprites[0] = sprite(WATER_STILL);
            sprites[1] = sprite(WATER_FLOW);
            sprites[2] = sprite(WATER_OVERLAY);
        } else if (fluid == FluidRegistry.LAVA) {
            sprites[0] = sprite(LAVA_STILL);
            sprites[1] = sprite(LAVA_FLOW);
            sprites[2] = null;
        } else {
            sprites[0] = getTexture(fluid.getStill());
            sprites[1] = getTexture(fluid.getFlowing());

            ResourceLocation overlay = fluid.getOverlay();
            sprites[2] = overlay != null ? getTexture(overlay) : null;
        }

        return sprites;
    }
}
