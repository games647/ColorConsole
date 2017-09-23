package com.github.games647.colorconsole.bukkit;

import com.github.games647.colorconsole.common.ColorAppender;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ColorPluginAppender extends ColorAppender {

    public ColorPluginAppender(Appender oldAppender, FileConfiguration config, Map<String, String> levelColors) {
        super(oldAppender
                , config.getStringList("hide-messages")
                , config.getBoolean("colorPluginTag")
                , config.getBoolean("truncateColor")
                , config.getBoolean("colorMessage") ? levelColors : Collections.emptyMap());
    }

    @Override
    public LogEvent onAppend(LogEvent logEvent) {
        String oldMessage = logEvent.getMessage().getFormattedMessage();
        Message newMessage = new SimpleMessage(formatter.colorizePluginTag(oldMessage, logEvent.getLevel().name()));
        return clone(logEvent, logEvent.getLoggerName(), newMessage);
    }

    @Override
    protected Collection<String> loadPluginNames() {
        return Stream.of(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .collect(Collectors.toSet());
    }
}
