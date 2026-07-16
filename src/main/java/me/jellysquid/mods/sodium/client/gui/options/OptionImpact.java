package me.jellysquid.mods.sodium.client.gui.options;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public enum OptionImpact {
    LOW(TextFormatting.GREEN, new TextComponentTranslation("vitrine.option_impact.low").getFormattedText()),
    MEDIUM(TextFormatting.YELLOW, new TextComponentTranslation("vitrine.option_impact.medium").getFormattedText()),
    HIGH(TextFormatting.GOLD, new TextComponentTranslation("vitrine.option_impact.high").getFormattedText()),
    EXTREME(TextFormatting.RED, new TextComponentTranslation("vitrine.option_impact.extreme").getFormattedText()),
    VARIES(TextFormatting.WHITE, new TextComponentTranslation("vitrine.option_impact.varies").getFormattedText());

    private final TextFormatting color;
    private final String text;

    OptionImpact(TextFormatting color, String text) {
        this.color = color;
        this.text = text;
    }

    public String toDisplayString() {
        return this.color + this.text;
    }
}
