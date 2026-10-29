package me.jellysquid.mods.sodium.client.gui.options.storage;

import javax.annotation.Nullable;

public interface OptionStorage<T> {
    T getData();

    void save();

    /**
     * @return A copy of the data holding only default values, which options read their defaults from, or null if the
     * storage cannot produce one without side effects
     */
    @Nullable
    default T getDefaults() {
        return null;
    }
}
