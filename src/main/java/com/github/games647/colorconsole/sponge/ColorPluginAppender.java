package com.github.games647.colorconsole.sponge;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.AnsiRenderer;
import org.fusesource.jansi.AnsiRenderer.Code;

public class ColorPluginAppender extends AbstractAppender {

    private final Appender oldAppender;

    private final ColorConsoleSponge plugin;

    private final String reset = Ansi.ansi().a(Attribute.RESET).toString();
    private final String defaultPluginColor;

    private final Set<String> ignoreMessages;

    public ColorPluginAppender(Appender oldAppender, ColorConsoleSponge plugin) {
        super(oldAppender.getName(), null, oldAppender.getLayout());

        this.plugin = plugin;

        this.oldAppender = oldAppender;
        this.defaultPluginColor = format(plugin.getConfig().getDefaultPluginColor());
        this.ignoreMessages = ImmutableSet.copyOf(plugin.getConfig().getHideMessages());
    }

    @Override
    public void append(LogEvent logEvent) {
        if (oldAppender.isStarted()) {
            String oldMessage = logEvent.getMessage().getFormattedMessage();
            for (String ignore : ignoreMessages) {
                if (oldMessage.contains(ignore)) {
                    return;
                }
            }

            String loggerName = logEvent.getLoggerName();
            String pluginColor = plugin.getConfig().getPluginColors().get(loggerName);
            if (pluginColor == null) {
                pluginColor = defaultPluginColor;
            } else {
                pluginColor = format(pluginColor);
            }

            String levelColor = "";
            if (plugin.getConfig().isColorLoggingLevel()) {
                levelColor = format(plugin.getConfig().getLevelColors().get(logEvent.getLevel().name()));
            }

            String newLoggerName = pluginColor + loggerName + reset + levelColor;

            LogEvent newEvent = new Log4jLogEvent(newLoggerName, logEvent.getMarker(), logEvent.getFQCN()
                    , logEvent.getLevel(), logEvent.getMessage(), logEvent.getThrown(), logEvent.getContextMap()
                    , logEvent.getContextStack(), logEvent.getThreadName(), logEvent.getSource(), logEvent.getMillis());
            oldAppender.append(newEvent);
        }
    }

    public Appender getOldAppender() {
        return oldAppender;
    }

    private String format(String pluginFormat) {
        String[] formatParts = pluginFormat.split(" ");
        Ansi ansi = Ansi.ansi();
        for (String format : formatParts) {
            for (Code ansiCode : AnsiRenderer.Code.values()) {
                if (ansiCode.name().equalsIgnoreCase(format)) {
                    if (ansiCode.isAttribute()) {
                        ansi.a(ansiCode.getAttribute());
                    } else if (ansiCode.isBackground()) {
                        ansi.bg(ansiCode.getColor());
                    } else {
                        ansi.fg(ansiCode.getColor());
                    }
                }
            }

            if ("blink".equalsIgnoreCase(format)) {
                ansi.a(Attribute.BLINK_SLOW);
                continue;
            }

            if ("strikethrough".equalsIgnoreCase(format)) {
                ansi.a(Attribute.STRIKETHROUGH_ON);
                continue;
            }

            if ("hidden".equalsIgnoreCase(format)) {
                ansi.a(Attribute.CONCEAL_OFF);
                continue;
            }

            if ("dim".equalsIgnoreCase(format)) {
                ansi.a(Attribute.INTENSITY_FAINT);
                continue;
            }

            if ("reverse".equalsIgnoreCase(format)) {
                ansi.a(Attribute.NEGATIVE_ON);
                continue;
            }

            for (Ansi.Color color : Ansi.Color.values()) {
                if (format.equalsIgnoreCase(color.name())) {
                    ansi.fg(color);
                    break;
                }
            }
        }

        return ansi.toString();
    }
}
