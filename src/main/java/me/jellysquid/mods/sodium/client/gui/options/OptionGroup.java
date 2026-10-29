package me.jellysquid.mods.sodium.client.gui.options;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OptionGroup {
    private final ITextComponent name;
    private final ImmutableList<Option<?>> options;

    private OptionGroup(ITextComponent name, ImmutableList<Option<?>> options) {
        this.name = name;
        this.options = options;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    /**
     * @return The heading shown above the group, or null if the group is only separated by spacing
     */
    @Nullable
    public ITextComponent getName() {
        return this.name;
    }

    public ImmutableList<Option<?>> getOptions() {
        return this.options;
    }

    public static class Builder {
        private ITextComponent name;
        private final List<Option<?>> options = new ArrayList<>();

        public Builder setName(ITextComponent name) {
            Validate.notNull(name, "Argument must not be null");

            this.name = name;

            return this;
        }

        public Builder add(Option<?> option) {
            this.options.add(option);

            return this;
        }

        public OptionGroup build() {
            Validate.notEmpty(this.options, "At least one option must be specified");

            return new OptionGroup(this.name, ImmutableList.copyOf(this.options));
        }
    }
}
