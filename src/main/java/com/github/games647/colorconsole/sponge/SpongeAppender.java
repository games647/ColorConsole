package com.github.games647.colorconsole.sponge;

import com.github.games647.colorconsole.common.ColorAppender;

import java.util.Collection;

import org.apache.logging.log4j.core.Appender;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import static java.util.stream.Collectors.toSet;

public class SpongeAppender extends ColorAppender {

    public SpongeAppender(Appender oldAppender, Collection<String> hideMessages, boolean truncateCol) {
        super(oldAppender, hideMessages, truncateCol);
    }

    @Override
    protected Collection<String> loadPluginNames() {
        return Sponge.getPluginManager().getPlugins().stream()
                .map(PluginContainer::getId)
                .collect(toSet());
    }
}
