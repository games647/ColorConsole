package com.github.games647.colorconsole.bukkit;

import com.github.games647.colorconsole.common.ColorAppender;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import static java.util.stream.Collectors.toSet;

public class ColorPluginAppender extends ColorAppender {

    private static final Set<String> disabledPrefix = Sets.newHashSet(
            "net.minecraft",
            "Minecraft",
            "com.mojang",
            "com.sk89q",
            "ru.tehkode",
            "Minecraft.AWE"
    );

    private final boolean isVanillaAppender;

    public ColorPluginAppender(Appender oldAppender, Collection<String> hideMessage, boolean truncateColor) {
        super(oldAppender, hideMessage, truncateColor);
        this.isVanillaAppender = "QueueLogAppender".equals(oldAppender.getClass().getSimpleName());
    }

    @Override
    public LogEvent onAppend(LogEvent logEvent) {
        String oldMessage = logEvent.getMessage().getFormattedMessage();
        if (logEvent.getLoggerName().isEmpty()) {
            // ignore non logging messages like command output
            return logEvent;
        }

        String prefix = '[' + logEvent.getLoggerName() + "] ";

        //PaperSpigot append prefix
        if (!isVanillaAppender
                && disabledPrefix.stream().noneMatch(disabled -> logEvent.getLoggerName().startsWith(disabled))) {
            oldMessage = prefix + oldMessage;
        }

        String message = formatter.colorizePluginTag(oldMessage);
        Message newMessage = new SimpleMessage(message);
        return clone(logEvent, logEvent.getLoggerName(), newMessage);
    }

    @Override
    protected Collection<String> loadPluginNames() {
        return Stream.of(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .collect(toSet());
    }
}
