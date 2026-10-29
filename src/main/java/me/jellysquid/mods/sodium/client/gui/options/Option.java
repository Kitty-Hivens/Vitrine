package me.jellysquid.mods.sodium.client.gui.options;

import me.jellysquid.mods.sodium.client.gui.options.control.Control;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;

public interface Option<T> {
    ITextComponent getNewName();

    String getName();

    ITextComponent getTooltip();

    /**
     * @return True if the pending value is the default, or if the option has no known default
     */
    boolean isAtDefault();

    /**
     * Sets the pending value to the default. Like any other change, it takes effect once applied.
     */
    void resetToDefault();

    OptionImpact getImpact();

    Control<T> getControl();

    T getValue();

    void setValue(T value);

    void reset();

    OptionStorage<?> getStorage();

    boolean isAvailable();

    boolean hasChanged();

    void applyChanges();

    Collection<OptionFlag> getFlags();
}
