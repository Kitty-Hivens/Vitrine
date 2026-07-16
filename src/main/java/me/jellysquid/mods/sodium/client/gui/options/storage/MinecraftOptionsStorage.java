package me.jellysquid.mods.sodium.client.gui.options.storage;

import dev.hivens.vitrine.Vitrine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class MinecraftOptionsStorage implements OptionStorage<GameSettings> {
    private final Minecraft client;

    public MinecraftOptionsStorage() {
        this.client = Minecraft.getMinecraft();
    }

    @Override
    public GameSettings getData() {
        return this.client.gameSettings;
    }

    @Override
    public void save() {
        this.getData().saveOptions();

        Vitrine.logger().info("Flushed changes to Minecraft configuration");
    }
}
