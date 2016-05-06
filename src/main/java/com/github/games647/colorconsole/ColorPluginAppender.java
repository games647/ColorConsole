package com.github.games647.colorconsole;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

public class ColorPluginAppender extends AbstractAppender {

    private final Appender oldAppender;

    private final ColorConsole plugin;

    private final String reset = Ansi.ansi().a(Attribute.RESET).toString();
    private final String defaultPluginColor;

    public ColorPluginAppender(Appender oldAppender, ColorConsole plugin) {
        super(oldAppender.getName(), null, oldAppender.getLayout());

        this.plugin = plugin;

        this.oldAppender = oldAppender;
        this.defaultPluginColor = format(plugin.getConfig().getString("PLUGIN"));
    }

    @Override
    public void append(LogEvent event) {
        if (oldAppender.isStarted()) {
            Message newMessage = new SimpleMessage(colorizePluginTag(event.getMessage().getFormattedMessage()
                    , event.getLevel().name()));

            LogEvent newEvent = new Log4jLogEvent(event.getLoggerName(), event.getMarker(), event.getFQCN()
                    , event.getLevel(), newMessage, event.getThrown(), event.getContextMap()
                    , event.getContextStack(), event.getThreadName(), event.getSource(), event.getMillis());
            oldAppender.append(newEvent);
        }
    }

    public Appender getOldAppender() {
        return oldAppender;
    }

    private String colorizePluginTag(String message, String level) {
        if (!message.contains("[") || !message.contains("]")) {
            return message;
        }

        String levelColor = format(plugin.getConfig().getString(level));

        int startTag = message.indexOf('[') + 1;
        int endTag = message.indexOf(']', startTag);

        String pluginName = message.substring(startTag, endTag);
        String pluginColor = plugin.getConfig().getString("P-" + pluginName, defaultPluginColor);
        if (pluginColor == null) {
            pluginColor = defaultPluginColor;
        } else {
            pluginColor = format(pluginColor);
        }

        return reset + '[' + pluginColor + pluginName + reset + ']' + levelColor + message.substring(endTag + 1);
    }

    private String format(String pluginFormat) {
        String[] formatParts = pluginFormat.split(" ");
        Ansi ansi = Ansi.ansi();
        for (String format : formatParts) {
            if (format.equalsIgnoreCase("blink")) {
                ansi.a(Attribute.BLINK_SLOW);
                continue;
            }

            if (format.equalsIgnoreCase("bold")) {
                ansi.a(Attribute.INTENSITY_BOLD);
                continue;
            }

            if (format.equalsIgnoreCase("underline")) {
                ansi.a(Attribute.UNDERLINE);
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
