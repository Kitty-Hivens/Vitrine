package me.jellysquid.mods.sodium.client.gui.widgets;

import me.jellysquid.mods.sodium.client.gui.Palette;
import me.jellysquid.mods.sodium.client.util.Dim2i;
import net.minecraft.util.text.ITextComponent;

/**
 * A page entry in the options screen's sidebar. The selected page is marked by an accent bar along its left edge.
 */
public class TabWidget extends AbstractWidget {
    private static final int ACCENT_WIDTH = 2;

    private final Dim2i dim;
    private final ITextComponent label;
    private final Runnable action;
    private final boolean selected;

    public TabWidget(Dim2i dim, ITextComponent label, boolean selected, Runnable action) {
        this.dim = dim;
        this.label = label;
        this.selected = selected;
        this.action = action;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        boolean hovered = this.dim.containsCursor(mouseX, mouseY);

        this.drawRect(this.dim.getOriginX(), this.dim.getOriginY(), this.dim.getLimitX(), this.dim.getLimitY(), (hovered || this.selected) ? Palette.PANEL_HOVERED : Palette.PANEL);

        if (this.selected) {
            this.drawRect(this.dim.getOriginX(), this.dim.getOriginY(), this.dim.getOriginX() + ACCENT_WIDTH, this.dim.getLimitY(), Palette.ACCENT);
        }

        this.drawString(this.label.getFormattedText(), this.dim.getOriginX() + 8, this.dim.getCenterY() - 4, (hovered || this.selected) ? Palette.TEXT : Palette.TEXT_SECONDARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.dim.containsCursor(mouseX, mouseY)) {
            this.action.run();
            this.playClickSound();

            return true;
        }

        return false;
    }
}
