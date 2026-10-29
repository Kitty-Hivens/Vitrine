package me.jellysquid.mods.sodium.client.gui.options.control;

import me.jellysquid.mods.sodium.client.gui.Palette;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.utils.Rect2i;
import me.jellysquid.mods.sodium.client.gui.widgets.AbstractWidget;
import me.jellysquid.mods.sodium.client.util.Dim2i;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class ControlElement<T> extends AbstractWidget {
    protected final Option<T> option;

    protected final Dim2i dim;

    protected boolean hovered;

    private Rect2i resetBounds;

    public ControlElement(Option<T> option, Dim2i dim) {
        this.option = option;
        this.dim = dim;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        String name = this.option.getName();
        String label;

        if (this.hovered && this.font.getStringWidth(name) > (this.dim.getWidth() - this.option.getControl().getMaxWidth())) {
            name = name.substring(0, Math.min(name.length(), 10)) + "...";
        }

        if (this.option.isAvailable()) {
            if (this.option.hasChanged()) {
                label = TextFormatting.ITALIC + name + " *";
            } else {
                label = TextFormatting.WHITE + name;
            }
        } else {
            label = String.valueOf(TextFormatting.GRAY) + TextFormatting.STRIKETHROUGH + name;
        }

        this.hovered = this.dim.containsCursor(mouseX, mouseY);

        // The list draws one panel behind every row, a row only darkens it while hovered
        if (this.hovered) {
            this.drawRect(this.dim.getOriginX(), this.dim.getOriginY(), this.dim.getLimitX(), this.dim.getLimitY(), Palette.PANEL_HOVERED);
        }

        this.drawString(label, this.dim.getOriginX() + 6, this.dim.getCenterY() - 4, Palette.TEXT);

        this.renderResetButton(mouseX, mouseY);
    }

    private void renderResetButton(int mouseX, int mouseY) {
        this.resetBounds = null;

        if (!this.hovered || !this.option.isAvailable() || this.option.isAtDefault()) {
            return;
        }

        String text = I18n.format("vitrine.options.reset");
        int width = this.getStringWidth(text);
        int right = this.dim.getLimitX() - this.getControlWidth() - 12;

        this.resetBounds = new Rect2i(right - width - 3, this.dim.getOriginY() + 2, width + 6, this.dim.getHeight() - 4);

        boolean hovered = this.resetBounds.contains(mouseX, mouseY);
        this.drawString(text, right - width, this.dim.getCenterY() - 4, hovered ? Palette.TEXT : Palette.TEXT_SECONDARY);
    }

    /**
     * @return How far the control reaches in from the right edge of the row
     */
    protected int getControlWidth() {
        return this.option.getControl().getMaxWidth();
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.resetBounds != null && this.resetBounds.contains((int) mouseX, (int) mouseY)) {
            this.option.resetToDefault();
            this.playClickSound();

            return true;
        }

        return this.onMouseClicked(mouseX, mouseY, button);
    }

    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public Option<T> getOption() {
        return this.option;
    }

    public Dim2i getDimensions() {
        return this.dim;
    }
}
