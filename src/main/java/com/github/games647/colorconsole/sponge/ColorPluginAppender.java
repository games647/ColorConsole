package com.github.games647.colorconsole.sponge;

import com.github.games647.colorconsole.common.ColorAppender;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

public class ColorPluginAppender extends ColorAppender {

    public ColorPluginAppender(Appender oldAppender, ColorConsoleConfig config) {
        super(oldAppender, config.getHideMessages(), config.isColorPluginTag(), config.isTruncateColor()
                , config.isColorMessage() ? config.getLevelColors() : Collections.emptyMap());
    }

    @Override
    public LogEvent onAppend(LogEvent logEvent) {
        String newLoggerName = formatter.colorizePluginName(logEvent.getLoggerName());
        return clone(logEvent, newLoggerName, logEvent.getMessage());
    }

    @Override
    protected Collection<String> loadPluginNames() {
        return Sponge.getPluginManager().getPlugins().stream()
                .map(PluginContainer::getId)
                .collect(Collectors.toSet());
    }
}
