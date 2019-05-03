package com.github.games647.colorconsole.bungee;

import com.github.games647.colorconsole.common.ColorAppender;

import java.util.Collection;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import static java.util.stream.Collectors.toSet;

public class ColorPluginAppender extends ColorAppender {

    private static final String PROXY_PREFIX = "BungeeCord";

    public ColorPluginAppender(Appender oldAppender, Collection<String> hideMessages, boolean truncateCol) {
        super(oldAppender, hideMessages, truncateCol);
    }

    @Override
    public LogEvent onAppend(LogEvent logEvent) {
        String message = logEvent.getMessage().getFormattedMessage();
        String loggerName = logEvent.getLoggerName();
        if (logEvent.getLoggerName().isEmpty()) {
            // ignore non logging messages like command output
            return logEvent;
        }

        //old message + potential prefix and color codes
        StringBuilder msgBuilder = new StringBuilder(message.length() + loggerName.length() + 10);
        if (!PROXY_PREFIX.equals(loggerName)) {
            msgBuilder.append('[')
                    .append(formatter.colorizePluginName(loggerName))
                    .append("] ");
            message = msgBuilder.append(message).toString();
        }

        Message newMessage = new SimpleMessage(message);
        return clone(logEvent, loggerName, newMessage);
    }

    @Override
    protected Collection<String> loadPluginNames() {
        return ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .map(Plugin::getDescription)
                .map(PluginDescription::getName)
                .collect(toSet());
    }
}
