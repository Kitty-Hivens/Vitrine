package me.jellysquid.mods.sodium.client.gui;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.named.AttackIndicator;
import me.jellysquid.mods.sodium.client.gui.options.named.GraphicsMode;
import me.jellysquid.mods.sodium.client.gui.options.named.ParticleMode;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawChunkRenderBackend;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SodiumGameOptionPages {
    private static final SodiumOptionsStorage sodiumOpts = new SodiumOptionsStorage();
    private static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();

    public static OptionPage general() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.renderDistance"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.view_distance.tooltip"))
                        .setControl(option -> new SliderControl(option, 2, 32, 1, ControlValueFormatter.quantity("options.chunks")))
                        .setBinding((options, value) -> options.renderDistanceChunks = value, options -> options.renderDistanceChunks)
                        .setDefault(12)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.gamma"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.brightness.tooltip"))
                        .setControl(opt -> new SliderControl(opt, 0, 100, 1, ControlValueFormatter.brightness()))
                        .setBinding((opts, value) -> opts.gammaSetting = value * 0.01F, (opts) -> (int) (opts.gammaSetting / 0.01F))
                        .setDefault(0)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.clouds.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.clouds.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.quality.enableClouds = value, (opts) -> opts.quality.enableClouds)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.guiScale"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.gui_scale.tooltip"))
                        .setControl(option -> new SliderControl(option, 0, 3, 1, ControlValueFormatter.guiScale()))
                        .setBinding((opts, value) -> {
                            opts.guiScale = value;

                            // Resizing our window
                            if(Minecraft.getMinecraft().currentScreen instanceof SodiumOptionsGUI) {
                                Minecraft.getMinecraft().displayGuiScreen(new SodiumOptionsGUI(((SodiumOptionsGUI) Minecraft.getMinecraft().currentScreen).prevScreen));
                            }
                        }, opts -> opts.guiScale)
                        .setDefault(0)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.fullscreen"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.fullscreen.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> {
                            opts.fullScreen = value;

                            Minecraft client = Minecraft.getMinecraft();

                            if (client.isFullScreen() != opts.fullScreen) {
                                client.toggleFullscreen();

                                // The client might not be able to enter full-screen mode
                                opts.fullScreen = client.isFullScreen();
                            }
                        }, (opts) -> opts.fullScreen)
                        .setDefault(false)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.vsync"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.v_sync.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> {
                            opts.enableVsync = value;
                            Display.setVSyncEnabled(opts.enableVsync);
                        }, opts -> opts.enableVsync)
                        .setDefault(true)
                        .setImpact(OptionImpact.VARIES)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.framerateLimit"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.fps_limit.tooltip"))
                        .setControl(option -> new SliderControl(option, 5, 260, 5, ControlValueFormatter.fpsLimit()))
                        .setBinding((opts, value) -> opts.limitFramerate = value, opts -> opts.limitFramerate)
                        .setDefault(120)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.viewBobbing"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.view_bobbing.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.viewBobbing = value, opts -> opts.viewBobbing)
                        .setDefault(true)
                        .build())
                .add(OptionImpl.createBuilder(AttackIndicator.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.attackIndicator"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.attack_indicator.tooltip"))
                        .setControl(opts -> new CyclingControl<>(opts, AttackIndicator.class, new ITextComponent[] { new TextComponentTranslation(AttackIndicator.OFF.getTranslationKey()), new TextComponentTranslation(AttackIndicator.CROSSHAIR.getTranslationKey()), new TextComponentTranslation(AttackIndicator.HOTBAR.getTranslationKey()) }))
                        .setBinding((opts, value) -> opts.attackIndicator = value.getId(), (opts) -> AttackIndicator.byId(opts.attackIndicator))
                        .setDefault(AttackIndicator.CROSSHAIR)
                        .build())
                .build());

        return new OptionPage(new TextComponentTranslation("stat.generalButton"), ImmutableList.copyOf(groups));
    }

    public static OptionPage quality() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(GraphicsMode.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.graphics"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.graphics_quality.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, GraphicsMode.class))
                        .setBinding(
                                (opts, value) -> opts.fancyGraphics = value.isFancy(),
                                opts -> GraphicsMode.fromBoolean(opts.fancyGraphics))
                        .setDefault(GraphicsMode.FANCY)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName(new TextComponentTranslation("options.renderClouds"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.clouds_quality.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.cloudQuality = value, opts -> opts.quality.cloudQuality)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName(new TextComponentTranslation("soundCategory.weather"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.weather_quality.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.weatherQuality = value, opts -> opts.quality.weatherQuality)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.leaves_quality.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.leaves_quality.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
                        .setBinding((opts, value) -> opts.quality.leavesQuality = value, opts -> opts.quality.leavesQuality)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(ParticleMode.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.particles"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.particle_quality.tooltip"))
                        .setControl(opt -> new CyclingControl<>(opt, ParticleMode.class))
                        .setBinding((opts, value) -> opts.particleSetting = value.ordinal(), (opts) -> ParticleMode.fromOrdinal(opts.particleSetting))
                        .setDefault(ParticleMode.ALL)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(SodiumGameOptions.LightingQuality.class, sodiumOpts)
                        .setName(new TextComponentTranslation("options.ao"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.smooth_lighting.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumGameOptions.LightingQuality.class))
                        .setBinding((opts, value) -> opts.quality.smoothLighting = value, opts -> opts.quality.smoothLighting)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                // TODO
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.biome_blend.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.biome_blend.tooltip"))
                        .setControl(option -> new SliderControl(option, 0, 7, 1, ControlValueFormatter.quantityOrDisabled("vitrine.options.biome_blend.value", "gui.none")))
                        .setBinding((opts, value) -> opts.quality.biomeBlendRadius = value, opts -> opts.quality.biomeBlendRadius)
                        .setImpact(OptionImpact.LOW)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.entity_distance.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.entity_distance.tooltip"))
                        .setControl(option -> new SliderControl(option, 50, 500, 25, ControlValueFormatter.percentage()))
                        .setBinding((opts, value) -> opts.quality.entityDistanceScaling = value / 100.0F, opts -> Math.round(opts.quality.entityDistanceScaling * 100.0F))
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.entityShadows"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.entity_shadows.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.entityShadows = value, opts -> opts.entityShadows)
                        .setDefault(true)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.vignette.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.vignette.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.quality.enableVignette = value, opts -> opts.quality.enableVignette)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());


        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName(new TextComponentTranslation("options.mipmapLevels"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.mipmap_levels.tooltip"))
                        .setControl(option -> new SliderControl(option, 0, 4, 1, ControlValueFormatter.multiplier()))
                        .setBinding((opts, value) -> opts.mipmapLevels = value, opts -> opts.mipmapLevels)
                        .setDefault(4)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build())
                .build());


        return new OptionPage(new TextComponentTranslation("vitrine.options.pages.quality"), ImmutableList.copyOf(groups));
    }

    public static OptionPage animations() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.animations.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.animations.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.animations.enabled = value, opts -> opts.animations.enabled)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.animate_only_visible_textures.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.animate_only_visible_textures.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.advanced.animateOnlyVisibleTextures = value, opts -> opts.advanced.animateOnlyVisibleTextures)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .setName(new TextComponentTranslation("vitrine.options.animations.types"))
                .add(animationOption("water", (opts, value) -> opts.animations.water = value, opts -> opts.animations.water))
                .add(animationOption("lava", (opts, value) -> opts.animations.lava = value, opts -> opts.animations.lava))
                .add(animationOption("fire", (opts, value) -> opts.animations.fire = value, opts -> opts.animations.fire))
                .add(animationOption("portal", (opts, value) -> opts.animations.portal = value, opts -> opts.animations.portal))
                .add(animationOption("terrain", (opts, value) -> opts.animations.terrain = value, opts -> opts.animations.terrain))
                .add(animationOption("textures", (opts, value) -> opts.animations.textures = value, opts -> opts.animations.textures))
                .build());

        return new OptionPage(new TextComponentTranslation("vitrine.options.pages.animations"), ImmutableList.copyOf(groups));
    }

    private static Option<Boolean> animationOption(String name, BiConsumer<SodiumGameOptions, Boolean> setter, Function<SodiumGameOptions, Boolean> getter) {
        return OptionImpl.createBuilder(boolean.class, sodiumOpts)
                .setName(new TextComponentTranslation("vitrine.options.animations." + name + ".name"))
                .setTooltip(new TextComponentTranslation("vitrine.options.animations." + name + ".tooltip"))
                .setControl(TickBoxControl::new)
                .setImpact(OptionImpact.MEDIUM)
                .setBinding(setter, getter)
                .build();
    }

    public static OptionPage particles() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.particles.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.particles.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.particles.enabled = value, opts -> opts.particles.enabled)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.particles.rain_splash.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.particles.rain_splash.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.LOW)
                        .setBinding((opts, value) -> opts.particles.rainSplash = value, opts -> opts.particles.rainSplash)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.particles.block_break.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.particles.block_break.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.LOW)
                        .setBinding((opts, value) -> opts.particles.blockBreak = value, opts -> opts.particles.blockBreak)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.particles.block_breaking.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.particles.block_breaking.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.LOW)
                        .setBinding((opts, value) -> opts.particles.blockBreaking = value, opts -> opts.particles.blockBreaking)
                        .build())
                .build());

        OptionGroup.Builder vanillaTypes = OptionGroup.createBuilder()
                .setName(new TextComponentTranslation("vitrine.options.particles.types"));

        // Mods append their types to the vanilla enum through EnumHelper, after the last vanilla constant
        Map<String, OptionGroup.Builder> moddedTypes = new LinkedHashMap<>();

        for (EnumParticleTypes type : EnumParticleTypes.values()) {
            if (type.ordinal() <= EnumParticleTypes.SPIT.ordinal()) {
                vanillaTypes.add(particleTypeOption(type, new TextComponentTranslation("vitrine.options.particles.type." + type.getParticleName()), "vitrine.options.particles.type.tooltip"));
                continue;
            }

            String name = type.getParticleName();
            int separator = name.indexOf('_');
            ModContainer owner = separator > 0 ? Loader.instance().getIndexedModList().get(name.substring(0, separator)) : null;

            String label = owner != null ? name.substring(separator + 1) : name;
            ITextComponent heading = owner != null ? new TextComponentString(owner.getName()) : new TextComponentTranslation("vitrine.options.particles.types.other_mods");

            moddedTypes.computeIfAbsent(heading.getUnformattedText(), key -> OptionGroup.createBuilder().setName(heading))
                    .add(particleTypeOption(type, new TextComponentString(humanize(label)), "vitrine.options.particles.type.modded_tooltip"));
        }

        groups.add(vanillaTypes.build());

        for (OptionGroup.Builder group : moddedTypes.values()) {
            groups.add(group.build());
        }

        return new OptionPage(new TextComponentTranslation("vitrine.options.pages.particles"), ImmutableList.copyOf(groups));
    }

    private static Option<Boolean> particleTypeOption(EnumParticleTypes type, ITextComponent name, String tooltip) {
        return OptionImpl.createBuilder(boolean.class, sodiumOpts)
                .setName(name)
                .setTooltip(new TextComponentTranslation(tooltip))
                .setControl(TickBoxControl::new)
                .setBinding((opts, value) -> opts.particles.setTypeEnabled(type, value), opts -> opts.particles.isTypeEnabled(type))
                .build();
    }

    /**
     * Turns a particle id such as "fluffy_trail" or "fluffyTrail" into "Fluffy trail". Mods do not ship names for
     * their particle types, so the id is the best there is.
     */
    private static String humanize(String id) {
        String spaced = id.replace('_', ' ').replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT).trim();

        return spaced.isEmpty() ? id : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    public static OptionPage advanced() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.use_chunk_multidraw.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.use_chunk_multidraw.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.useChunkMultidraw = value, opts -> opts.advanced.useChunkMultidraw)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .setImpact(OptionImpact.EXTREME)
                        .setEnabled(MultidrawChunkRenderBackend.isSupported(sodiumOpts.getData().advanced.ignoreDriverBlacklist))
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.use_vertex_objects.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.use_vertex_objects.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.useVertexArrayObjects = value, opts -> opts.advanced.useVertexArrayObjects)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.use_block_face_culling.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.use_block_face_culling.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.advanced.useBlockFaceCulling = value, opts -> opts.advanced.useBlockFaceCulling)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.use_compact_vertex_format.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.use_compact_vertex_format.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.advanced.useCompactVertexFormat = value, opts -> opts.advanced.useCompactVertexFormat)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.use_fog_occlusion.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.use_fog_occlusion.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.useFogOcclusion = value, opts -> opts.advanced.useFogOcclusion)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.translucency_sorting.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.translucency_sorting.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.translucencySorting = value, opts -> opts.advanced.translucencySorting)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.use_entity_culling.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.use_entity_culling.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.MEDIUM)
                        .setBinding((opts, value) -> opts.advanced.useEntityCulling = value, opts -> opts.advanced.useEntityCulling)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.use_particle_culling.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.use_particle_culling.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.LOW)
                        .setBinding((opts, value) -> opts.advanced.useParticleCulling = value, opts -> opts.advanced.useParticleCulling)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.allow_direct_memory_access.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.allow_direct_memory_access.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.advanced.allowDirectMemoryAccess = value, opts -> opts.advanced.allowDirectMemoryAccess)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.ignore_driver_blacklist.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.ignore_driver_blacklist.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advanced.ignoreDriverBlacklist = value, opts -> opts.advanced.ignoreDriverBlacklist)
                        .build()
                )
                .build());

        return new OptionPage(new TextComponentTranslation("vitrine.options.pages.advanced"), ImmutableList.copyOf(groups));
    }

    public static OptionPage performance() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.chunk_update_threads.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.chunk_update_threads.tooltip"))
                        .setControl(o -> new SliderControl(o, 0, Runtime.getRuntime().availableProcessors(), 1, ControlValueFormatter.quantityOrDisabled("vitrine.options.threads.value", "vitrine.options.default")))
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.performance.chunkBuilderThreads = value, opts -> opts.performance.chunkBuilderThreads)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumOpts)
                        .setName(new TextComponentTranslation("vitrine.options.always_defer_chunk_updates.name"))
                        .setTooltip(new TextComponentTranslation("vitrine.options.always_defer_chunk_updates.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setImpact(OptionImpact.HIGH)
                        .setBinding((opts, value) -> opts.performance.alwaysDeferChunkUpdates = value, opts -> opts.performance.alwaysDeferChunkUpdates)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .build());

        return new OptionPage(new TextComponentTranslation("vitrine.options.pages.performance"), ImmutableList.copyOf(groups));
    }
}