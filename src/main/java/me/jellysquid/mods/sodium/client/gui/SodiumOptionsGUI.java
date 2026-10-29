package me.jellysquid.mods.sodium.client.gui;

import dev.hivens.vitrine.Vitrine;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import me.jellysquid.mods.sodium.client.gui.utils.Drawable;
import me.jellysquid.mods.sodium.client.gui.utils.Element;
import me.jellysquid.mods.sodium.client.gui.utils.Rect2i;
import me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget;
import me.jellysquid.mods.sodium.client.gui.widgets.TabWidget;
import me.jellysquid.mods.sodium.client.util.Dim2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class SodiumOptionsGUI extends GuiScreen {
    private static final int PADDING = 6;
    private static final int ROW_HEIGHT = 18;
    private static final int BAR_HEIGHT = 20;
    private static final int SEPARATOR_HEIGHT = 5;

    private static final int SCROLLBAR_WIDTH = 2;
    private static final int SCROLLBAR_GAP = 2;
    private static final int SCROLL_STEP = ROW_HEIGHT * 2;

    private static final int TOOLTIP_MAX_WIDTH = 240;
    private static final long TOOLTIP_DELAY_MS = 500;

    // Tabs and the buttons along the edges of the screen. Rebuilt while a click is being dispatched, hence the copy.
    private final List<Element> children = new CopyOnWriteArrayList<>();
    private final List<Drawable> drawable = new ArrayList<>();

    private final List<OptionPage> pages = new ArrayList<>();

    // The scrolling list, laid out from the top of the viewport as if it were not scrolled
    private final List<ControlElement<?>> controls = new ArrayList<>();
    private final List<Heading> headings = new ArrayList<>();

    public final GuiScreen prevScreen;

    private OptionPage currentPage;

    private Dim2i searchBounds, viewport, scrollbarHitbox;
    private GuiTextField searchField;

    private int contentHeight;
    private int rowWidth;
    private int scrollOffset;

    private boolean draggingScrollbar;
    private int dragStartMouseY, dragStartOffset;

    private FlatButtonWidget applyButton, closeButton, undoButton;
    private FlatButtonWidget donateButton, hideDonateButton;

    private boolean hasPendingChanges;
    private ControlElement<?> hoveredElement;
    private long hoverStartTime;

    public SodiumOptionsGUI(GuiScreen prevScreen) {
        super();

        this.prevScreen = prevScreen;

        this.pages.add(SodiumGameOptionPages.general());
        this.pages.add(SodiumGameOptionPages.quality());
        this.pages.add(SodiumGameOptionPages.animations());
        this.pages.add(SodiumGameOptionPages.particles());
        this.pages.add(SodiumGameOptionPages.performance());
        this.pages.add(SodiumGameOptionPages.advanced());
    }

    public void setPage(OptionPage page) {
        this.currentPage = page;
        this.scrollOffset = 0;

        if (this.searchField != null) {
            this.searchField.setText("");
            this.searchField.setFocused(false);
        }

        this.rebuildGUI();
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.rebuildGUI();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        this.searchField.updateCursorCounter();
    }

    private void rebuildGUI() {
        this.children.clear();
        this.drawable.clear();

        if (this.currentPage == null) {
            if (this.pages.isEmpty()) {
                throw new IllegalStateException("No pages are available?!");
            }

            // Just use the first page for now
            this.currentPage = this.pages.get(0);
        }

        boolean donateVisible = !Vitrine.options().notifications.hideDonationButton;

        this.rebuildSearchField(donateVisible ? this.width - 134 : this.width - PADDING);
        int sidebarWidth = this.rebuildSidebar();

        int top = PADDING + BAR_HEIGHT + PADDING;
        int left = PADDING + sidebarWidth + PADDING;
        this.viewport = new Dim2i(left, top, this.width - PADDING - left, this.height - 26 - PADDING - top);

        this.rebuildContent();

        this.undoButton = new FlatButtonWidget(new Dim2i(this.width - 211, this.height - 26, 65, 20), new TextComponentTranslation("vitrine.options.buttons.undo").getFormattedText(), this::undoChanges);
        this.applyButton = new FlatButtonWidget(new Dim2i(this.width - 142, this.height - 26, 65, 20), new TextComponentTranslation("vitrine.options.buttons.apply").getFormattedText(), this::applyChanges);
        this.closeButton = new FlatButtonWidget(new Dim2i(this.width - 73, this.height - 26, 65, 20), new TextComponentTranslation("gui.done").getFormattedText(), this::onClose);
        this.donateButton = new FlatButtonWidget(new Dim2i(this.width - 128, PADDING, 100, 20), new TextComponentTranslation("vitrine.options.buttons.donate").getFormattedText(), this::openDonationPage);
        this.hideDonateButton = new FlatButtonWidget(new Dim2i(this.width - 26, PADDING, 20, 20), "x", this::hideDonationButton);

        if (!donateVisible) {
            this.donateButton.setVisible(false);
            this.hideDonateButton.setVisible(false);
        }

        this.children.add(this.undoButton);
        this.children.add(this.applyButton);
        this.children.add(this.closeButton);
        this.children.add(this.donateButton);
        this.children.add(this.hideDonateButton);

        for (Element element : this.children) {
            if (element instanceof Drawable) {
                this.drawable.add((Drawable) element);
            }
        }
    }

    private void rebuildSearchField(int right) {
        this.searchBounds = new Dim2i(PADDING, PADDING, right - PADDING, BAR_HEIGHT);

        int fieldWidth = this.searchBounds.getWidth() - (PADDING * 2);

        // Typing rebuilds the screen too, and a new field would lose the cursor position
        if (this.searchField != null && this.searchField.getWidth() == fieldWidth) {
            return;
        }

        String text = "";
        boolean focused = false;

        if (this.searchField != null) {
            text = this.searchField.getText();
            focused = this.searchField.isFocused();
        }

        // The field draws only text and cursor, the panel behind it is ours
        this.searchField = new GuiTextField(0, this.fontRenderer, this.searchBounds.getOriginX() + PADDING, this.searchBounds.getCenterY() - 4, fieldWidth, this.fontRenderer.FONT_HEIGHT);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(64);
        this.searchField.setTextColor(Palette.TEXT);
        this.searchField.setText(text);
        this.searchField.setFocused(focused);
    }

    /**
     * @return The width of the sidebar
     */
    private int rebuildSidebar() {
        int width = 0;

        for (OptionPage page : this.pages) {
            width = Math.max(width, this.fontRenderer.getStringWidth(page.getName()));
        }

        width += 8 + PADDING;

        int x = PADDING;
        int y = PADDING + BAR_HEIGHT + PADDING;

        boolean searching = this.isSearching();

        for (OptionPage page : this.pages) {
            this.children.add(new TabWidget(new Dim2i(x, y, width, ROW_HEIGHT), page.getNewName(), !searching && page == this.currentPage, () -> this.setPage(page)));
            y += ROW_HEIGHT;
        }

        return width;
    }

    private void rebuildContent() {
        this.controls.clear();
        this.headings.clear();

        List<Section> sections = this.isSearching() ? this.findMatches() : sectionsOf(this.currentPage);

        this.contentHeight = layout(sections, null, null, 0, 0, 0);

        int rowWidth = this.viewport.getWidth();

        if (this.contentHeight > this.viewport.getHeight()) {
            rowWidth -= SCROLLBAR_WIDTH + SCROLLBAR_GAP;
        }

        layout(sections, this.controls, this.headings, this.viewport.getOriginX(), this.viewport.getOriginY(), rowWidth);
        this.rowWidth = rowWidth;

        this.scrollbarHitbox = new Dim2i(this.viewport.getLimitX() - SCROLLBAR_WIDTH - SCROLLBAR_GAP, this.viewport.getOriginY(), SCROLLBAR_WIDTH + SCROLLBAR_GAP, this.viewport.getHeight());
        this.setScrollOffset(this.scrollOffset);
    }

    /**
     * Places every section one below the other. With null outputs this only measures.
     * @return The total height of the laid out sections
     */
    private static int layout(List<Section> sections, List<ControlElement<?>> controls, List<Heading> headings, int x, int top, int width) {
        int y = 0;
        boolean first = true;

        for (Section section : sections) {
            if (section.heading != null) {
                if (headings != null) {
                    headings.add(new Heading(section.heading, new Dim2i(x, top + y, width, ROW_HEIGHT), !first, toggleableOptions(section.options)));
                }

                y += ROW_HEIGHT;
            } else if (!first) {
                if (headings != null) {
                    headings.add(new Heading(null, new Dim2i(x, top + y, width, SEPARATOR_HEIGHT), true, Collections.emptyList()));
                }

                y += SEPARATOR_HEIGHT;
            }

            for (Option<?> option : section.options) {
                if (controls != null) {
                    controls.add(option.getControl().createElement(new Dim2i(x, top + y, width, ROW_HEIGHT)));
                }

                y += ROW_HEIGHT;
            }

            first = false;
        }

        return y;
    }

    /**
     * @return The switches a group heading toggles all at once, or none if the group has fewer than two
     */
    @SuppressWarnings("unchecked")
    private static List<Option<Boolean>> toggleableOptions(List<Option<?>> options) {
        List<Option<Boolean>> toggles = new ArrayList<>();

        for (Option<?> option : options) {
            if (option.getControl() instanceof TickBoxControl && option.isAvailable()) {
                toggles.add((Option<Boolean>) option);
            }
        }

        return toggles.size() >= 2 ? toggles : Collections.emptyList();
    }

    private static List<Section> sectionsOf(OptionPage page) {
        List<Section> sections = new ArrayList<>();

        for (OptionGroup group : page.getGroups()) {
            sections.add(new Section(group.getName(), group.getOptions()));
        }

        return sections;
    }

    private List<Section> findMatches() {
        String query = this.searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<Section> sections = new ArrayList<>();

        for (OptionPage page : this.pages) {
            List<Option<?>> matches = new ArrayList<>();

            for (Option<?> option : page.getOptions()) {
                String name = TextFormatting.getTextWithoutFormattingCodes(option.getName());

                if (name != null && name.toLowerCase(Locale.ROOT).contains(query)) {
                    matches.add(option);
                }
            }

            if (!matches.isEmpty()) {
                sections.add(new Section(page.getNewName(), matches));
            }
        }

        if (sections.isEmpty()) {
            sections.add(new Section(new TextComponentTranslation("vitrine.options.search.no_results"), new ArrayList<>()));
        }

        return sections;
    }

    private boolean isSearching() {
        return this.searchField != null && !this.searchField.getText().trim().isEmpty();
    }

    private int getMaxScrollOffset() {
        return Math.max(0, this.contentHeight - this.viewport.getHeight());
    }

    private void setScrollOffset(int offset) {
        this.scrollOffset = Math.max(0, Math.min(offset, this.getMaxScrollOffset()));
    }

    private void hideDonationButton() {
        SodiumGameOptions options = Vitrine.options();
        options.notifications.hideDonationButton = true;

        try {
            options.writeChanges();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }

        this.rebuildGUI();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        super.drawDefaultBackground();

        this.updateControls();

        this.renderSearchField(mouseX, mouseY);
        this.renderVersionLabel();
        this.renderContent(mouseX, mouseY, delta);
        this.renderScrollbar(mouseX, mouseY);

        for (Drawable drawable : this.drawable) {
            drawable.render(mouseX, mouseY, delta);
        }

        if (this.hoveredElement != null && Minecraft.getSystemTime() - this.hoverStartTime >= TOOLTIP_DELAY_MS) {
            this.renderOptionTooltip(this.hoveredElement);
        }
    }

    private void renderSearchField(int mouseX, int mouseY) {
        Dim2i bounds = this.searchBounds;
        boolean active = this.searchField.isFocused() || bounds.containsCursor(mouseX, mouseY);

        drawRect(bounds.getOriginX(), bounds.getOriginY(), bounds.getLimitX(), bounds.getLimitY(), active ? Palette.PANEL_HOVERED : Palette.PANEL);

        // Shadowed like the text the field draws once something is typed
        if (!this.searchField.isFocused() && this.searchField.getText().isEmpty()) {
            this.fontRenderer.drawStringWithShadow(I18n.format("vitrine.options.search"), bounds.getOriginX() + PADDING, bounds.getCenterY() - 4, Palette.TEXT_SECONDARY);
        }

        this.searchField.drawTextBox();
    }

    // Plain text on the button row, so it does not read as one more page in the sidebar
    private void renderVersionLabel() {
        this.fontRenderer.drawString(Vitrine.MODNAME + " " + Vitrine.getVersion(), PADDING + 2, this.height - 20, Palette.TEXT_SECONDARY);
    }

    private void renderContent(int mouseX, int mouseY, float delta) {
        // A cursor outside the viewport must not hover rows that are scrolled out of sight
        boolean inside = this.viewport.containsCursor(mouseX, mouseY) && !this.draggingScrollbar;
        int contentMouseX = inside ? mouseX : -1;
        int contentMouseY = inside ? mouseY + this.scrollOffset : -1;

        this.enableScissor(this.viewport);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -this.scrollOffset, 0.0F);

        // One panel behind the whole list, so groups are told apart by lines and headings rather than gaps
        drawRect(this.viewport.getOriginX(), this.viewport.getOriginY(), this.viewport.getOriginX() + this.rowWidth, this.viewport.getOriginY() + this.contentHeight, Palette.PANEL);

        for (Heading heading : this.headings) {
            this.renderHeading(heading);
        }

        for (ControlElement<?> control : this.controls) {
            control.render(contentMouseX, contentMouseY, delta);
        }

        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void renderHeading(Heading heading) {
        Dim2i dim = heading.dim;

        if (heading.divider) {
            int lineY = heading.text != null ? dim.getOriginY() : dim.getCenterY();
            drawRect(dim.getOriginX(), lineY, dim.getLimitX(), lineY + 1, Palette.DIVIDER);
        }

        if (heading.text != null) {
            this.fontRenderer.drawString(heading.text.getFormattedText(), dim.getOriginX() + 6, dim.getCenterY() - 4, Palette.TEXT_SECONDARY);
        }

        if (heading.toggles.isEmpty()) {
            return;
        }

        boolean all = heading.toggles.stream().allMatch(Option::getValue);
        boolean any = heading.toggles.stream().anyMatch(Option::getValue);
        int color = any ? Palette.ACCENT : Palette.CONTROL;

        Rect2i box = heading.getToggleBox();
        int x1 = box.getX();
        int y1 = box.getY();
        int x2 = x1 + box.getWidth();
        int y2 = y1 + box.getHeight();

        drawRect(x1, y1, x2, y1 + 1, color);
        drawRect(x1, y2 - 1, x2, y2, color);
        drawRect(x1, y1, x1 + 1, y2, color);
        drawRect(x2 - 1, y1, x2, y2, color);

        if (all) {
            drawRect(x1 + 2, y1 + 2, x2 - 2, y2 - 2, color);
        } else if (any) {
            // A bar rather than a full fill: only part of the group is on
            drawRect(x1 + 2, y1 + 4, x2 - 2, y2 - 4, color);
        }
    }

    private void renderScrollbar(int mouseX, int mouseY) {
        int maxOffset = this.getMaxScrollOffset();

        if (maxOffset == 0) {
            return;
        }

        int thumbHeight = this.getThumbHeight();
        int thumbY = this.viewport.getOriginY() + (int) ((long) (this.viewport.getHeight() - thumbHeight) * this.scrollOffset / maxOffset);
        int x = this.viewport.getLimitX() - SCROLLBAR_WIDTH;

        boolean active = this.draggingScrollbar || this.scrollbarHitbox.containsCursor(mouseX, mouseY);

        drawRect(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, active ? Palette.SCROLLBAR_ACTIVE : Palette.SCROLLBAR);
    }

    private int getThumbHeight() {
        int viewportHeight = this.viewport.getHeight();

        return Math.max(ROW_HEIGHT, (int) ((long) viewportHeight * viewportHeight / this.contentHeight));
    }

    private void enableScissor(Dim2i dim) {
        int scale = new ScaledResolution(this.mc).getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(dim.getOriginX() * scale, this.mc.displayHeight - (dim.getLimitY() * scale), dim.getWidth() * scale, dim.getHeight() * scale);
    }

    private void updateControls() {
        ControlElement<?> hovered = this.controls.stream()
                .filter(ControlElement::isHovered)
                .findFirst()
                .orElse(null);

        if (hovered != this.hoveredElement) {
            this.hoveredElement = hovered;
            this.hoverStartTime = Minecraft.getSystemTime();
        }

        boolean hasChanges = this.getAllOptions()
                .anyMatch(Option::hasChanged);

        this.applyButton.setEnabled(hasChanges);
        this.undoButton.setVisible(hasChanges);
        this.closeButton.setEnabled(!hasChanges);

        this.hasPendingChanges = hasChanges;
    }

    private Stream<Option<?>> getAllOptions() {
        return this.pages.stream()
                .flatMap(s -> s.getOptions().stream());
    }

    private void renderOptionTooltip(ControlElement<?> element) {
        Dim2i dim = element.getDimensions();

        int textPadding = 3;
        int boxPadding = 3;

        int boxWidth = Math.min(TOOLTIP_MAX_WIDTH, dim.getWidth());

        Option<?> option = element.getOption();
        List<String> tooltip = new ArrayList<>(this.fontRenderer.listFormattedStringToWidth(option.getTooltip().getFormattedText(), boxWidth - (textPadding * 2)));

        OptionImpact impact = option.getImpact();

        if (impact != null) {
            tooltip.add(TextFormatting.GRAY + I18n.format("vitrine.options.performance_impact_string", impact.toDisplayString()));
        }

        int boxHeight = (tooltip.size() * 12) + boxPadding;

        // Below the row it describes, or above it when that would run into the buttons at the bottom
        int rowTop = dim.getOriginY() - this.scrollOffset;
        int boxX = dim.getOriginX();
        int boxY = rowTop + dim.getHeight();

        if (boxY + boxHeight > this.height - 26 - PADDING) {
            boxY = rowTop - boxHeight;
        }

        drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, Palette.PANEL_HOVERED);

        for (int i = 0; i < tooltip.size(); i++) {
            this.fontRenderer.drawString(tooltip.get(i), boxX + textPadding, boxY + textPadding + (i * 12), Palette.TEXT);
        }
    }

    private void applyChanges() {
        final HashSet<OptionStorage<?>> dirtyStorages = new HashSet<>();
        final EnumSet<OptionFlag> flags = EnumSet.noneOf(OptionFlag.class);

        this.getAllOptions().forEach((option -> {
            if (!option.hasChanged()) {
                return;
            }

            option.applyChanges();

            flags.addAll(option.getFlags());
            dirtyStorages.add(option.getStorage());
        }));

        if (flags.contains(OptionFlag.REQUIRES_RENDERER_RELOAD)) {
            this.mc.renderGlobal.loadRenderers();
        }

        if (flags.contains(OptionFlag.REQUIRES_ASSET_RELOAD)) {
            this.mc.getTextureMapBlocks().setMipmapLevels(this.mc.gameSettings.mipmapLevels);
            this.mc.refreshResources();
        }

        for (OptionStorage<?> storage : dirtyStorages) {
            storage.save();
        }
    }

    private void undoChanges() {
        this.getAllOptions()
                .forEach(Option::reset);
    }

    private void openDonationPage() {
        URLUtils.open("https://caffeinemc.net/donate");
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.searchField.isFocused()) {
                this.searchField.setFocused(false);
            } else if (this.shouldCloseOnEsc()) {
                this.onClose();
            }

            return;
        }

        String query = this.searchField.getText();

        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            if (!query.equals(this.searchField.getText())) {
                this.scrollOffset = 0;
                this.rebuildGUI();
            }

            return;
        }

        if (keyCode == Keyboard.KEY_P && isShiftKeyDown()) {
            this.mc.displayGuiScreen(new GuiVideoSettings(this.prevScreen, this.mc.gameSettings));
        }
    }

    public boolean shouldCloseOnEsc() {
        return !this.hasPendingChanges;
    }

    public void onClose() {
        this.mc.displayGuiScreen(this.prevScreen);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();

        if (wheel != 0) {
            this.setScrollOffset(this.scrollOffset + (wheel > 0 ? -SCROLL_STEP : SCROLL_STEP));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        boolean inSearch = this.searchBounds.containsCursor(mouseX, mouseY);
        this.searchField.setFocused(inSearch);

        if (inSearch) {
            this.searchField.mouseClicked(mouseX, mouseY, mouseButton);

            return;
        }

        if (mouseButton == 0 && this.getMaxScrollOffset() > 0 && this.scrollbarHitbox.containsCursor(mouseX, mouseY)) {
            this.beginScrollbarDrag(mouseY);

            return;
        }

        this.children.forEach(element -> element.mouseClicked(mouseX, mouseY, mouseButton));

        if (this.viewport.containsCursor(mouseX, mouseY)) {
            if (mouseButton == 0 && this.toggleGroupAt(mouseX, mouseY + this.scrollOffset)) {
                return;
            }

            this.controls.forEach(element -> element.mouseClicked(mouseX, mouseY + this.scrollOffset, mouseButton));
        }
    }

    /**
     * Switches a whole group on, or off when every switch in it is already on.
     * @return True if the position was a group switch
     */
    private boolean toggleGroupAt(int x, int contentY) {
        for (Heading heading : this.headings) {
            if (heading.toggles.isEmpty() || !heading.getToggleHitbox().contains(x, contentY)) {
                continue;
            }

            boolean enable = !heading.toggles.stream().allMatch(Option::getValue);
            heading.toggles.forEach(option -> option.setValue(enable));

            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            return true;
        }

        return false;
    }

    private void beginScrollbarDrag(int mouseY) {
        int maxOffset = this.getMaxScrollOffset();
        int thumbHeight = this.getThumbHeight();
        int thumbY = this.viewport.getOriginY() + (int) ((long) (this.viewport.getHeight() - thumbHeight) * this.scrollOffset / maxOffset);

        // Clicking the track rather than the thumb moves the thumb under the cursor first
        if (mouseY < thumbY || mouseY >= thumbY + thumbHeight) {
            int track = this.viewport.getHeight() - thumbHeight;
            this.setScrollOffset((int) ((long) (mouseY - this.viewport.getOriginY() - (thumbHeight / 2)) * maxOffset / track));
        }

        this.draggingScrollbar = true;
        this.dragStartMouseY = mouseY;
        this.dragStartOffset = this.scrollOffset;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (this.draggingScrollbar) {
            int track = this.viewport.getHeight() - this.getThumbHeight();

            if (track > 0) {
                this.setScrollOffset(this.dragStartOffset + (int) ((long) (mouseY - this.dragStartMouseY) * this.getMaxScrollOffset() / track));
            }

            return;
        }

        this.children.forEach(element -> element.mouseDragged(mouseX, mouseY));
        this.controls.forEach(element -> element.mouseDragged(mouseX, mouseY + this.scrollOffset));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        this.draggingScrollbar = false;
    }

    private static class Section {
        private final ITextComponent heading;
        private final List<Option<?>> options;

        private Section(ITextComponent heading, List<Option<?>> options) {
            this.heading = heading;
            this.options = options;
        }
    }

    /**
     * A group heading, or with no text a plain separator between unnamed groups.
     */
    private static class Heading {
        private final ITextComponent text;
        private final Dim2i dim;
        private final boolean divider;
        private final List<Option<Boolean>> toggles;

        private Heading(ITextComponent text, Dim2i dim, boolean divider, List<Option<Boolean>> toggles) {
            this.text = text;
            this.dim = dim;
            this.divider = divider;
            this.toggles = toggles;
        }

        // Placed where a switch sits in an option row, so the group switch lines up with the ones below it
        private Rect2i getToggleBox() {
            return new Rect2i(this.dim.getLimitX() - 16, this.dim.getCenterY() - 5, 10, 10);
        }

        private Rect2i getToggleHitbox() {
            return new Rect2i(this.dim.getLimitX() - 22, this.dim.getOriginY(), 22, this.dim.getHeight());
        }
    }
}
