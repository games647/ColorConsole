package com.github.games647.colorconsole.sponge;

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

    public ColorPluginAppender(Appender oldAppender, ColorConsoleSponge plugin) {
        super(oldAppender.getName(), null, oldAppender.getLayout());

        this.plugin = plugin;

        this.oldAppender = oldAppender;
        this.defaultPluginColor = format(plugin.getConfig().getDefaultPluginColor());
    }

    @Override
    public void append(LogEvent event) {
        if (oldAppender.isStarted()) {
            String loggerName = event.getLoggerName();
            String pluginColor = plugin.getConfig().getPluginColors().get(loggerName);
            if (pluginColor == null) {
                pluginColor = defaultPluginColor;
            } else {
                pluginColor = format(pluginColor);
            }

            String levelColor = "";
            if (plugin.getConfig().isColorLoggingLevel()) {
                levelColor = format(plugin.getConfig().getLevelColors().get(event.getLevel().name()));
            }

            String newLoggerName = pluginColor + loggerName + reset + levelColor;

            LogEvent newEvent = new Log4jLogEvent(newLoggerName, event.getMarker(), event.getFQCN()
                    , event.getLevel(), event.getMessage(), event.getThrown(), event.getContextMap()
                    , event.getContextStack(), event.getThreadName(), event.getSource(), event.getMillis());
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
