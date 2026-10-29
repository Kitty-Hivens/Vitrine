package me.jellysquid.mods.sodium.client.gui;

/**
 * Every colour the options screen draws with. The screen is flat rectangles and the vanilla font only, so a colour
 * that is not listed here does not belong on it.
 */
public final class Palette {
    public static final int PANEL = 0x90000000;
    public static final int PANEL_HOVERED = 0xE0000000;
    public static final int PANEL_DISABLED = 0x60000000;

    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFFAAAAAA;
    public static final int TEXT_DISABLED = 0x90FFFFFF;

    public static final int ACCENT = 0xFF94E4D3;
    public static final int CONTROL = 0xFFFFFFFF;
    public static final int CONTROL_DISABLED = 0xFFAAAAAA;

    public static final int DIVIDER = 0x30FFFFFF;

    public static final int SCROLLBAR = 0x60FFFFFF;
    public static final int SCROLLBAR_ACTIVE = 0xA0FFFFFF;

    private Palette() {
    }
}
