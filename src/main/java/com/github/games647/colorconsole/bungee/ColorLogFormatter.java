package com.github.games647.colorconsole.bungee;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.AnsiRenderer;
import org.fusesource.jansi.AnsiRenderer.Code;

public class ColorLogFormatter extends Formatter {

    private final ColorConsoleBungee plugin;
    private final Formatter oldFormatter;

    private final DateFormat date = new SimpleDateFormat("HH:mm:ss");

    private final String reset = Ansi.ansi().a(Attribute.RESET).toString();
    private final String defaultPluginColor;

    public ColorLogFormatter(ColorConsoleBungee plugin, Formatter oldFormatter) {
        this.plugin = plugin;
        this.oldFormatter = oldFormatter;

        this.defaultPluginColor = format(plugin.getConfiguration().getString("PLUGIN"));
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder formatted = new StringBuilder();

        String levelName = record.getLevel().getName();
        String levelColor = "";
        if (plugin.getConfiguration().getBoolean("colorLoggingLevel")) {
            String log4JName = translateToLog4JName(record.getLevel());
            levelColor = format(plugin.getConfiguration().getString(log4JName));
        }

        formatted.append(levelColor);

        formatted.append(this.date.format(Long.valueOf(record.getMillis())));
        formatted.append(" [");
        formatted.append(record.getLevel().getName());
        formatted.append("] ");

        String message = oldFormatter.formatMessage(record);
        if (plugin.getConfiguration().getBoolean("colorPluginTag")) {
            message = colorizePluginTag(message, levelColor);
        }

        formatted.append(message);

        formatted.append('\n');
        if (record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            formatted.append(writer);
        }

        return formatted.toString();
    }

    public Formatter getOldFormatter() {
        return oldFormatter;
    }

    private String colorizePluginTag(String message, String levelColor) {
        if (!message.contains("[") || !message.contains("]")) {
            return message;
        }

        int startTag = message.indexOf('[') + 1;
        int endTag = message.indexOf(']', startTag);

        String pluginName = message.substring(startTag, endTag);
        String pluginColor = plugin.getConfiguration().getString("P-" + pluginName);
        if (pluginColor == null || pluginColor.isEmpty()) {
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

    private String translateToLog4JName(Level level) {
        if (level == Level.SEVERE) {
            return "ERROR";
        } else if (level == Level.WARNING) {
            return "WARN";
        } else if (level == Level.INFO) {
            return "INFO";
        } else if (level == Level.CONFIG) {
            return "DEBUG";
        } else {
            return "TRACE";
        }
    }
}
