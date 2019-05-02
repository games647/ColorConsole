package com.github.games647.colorconsole.bungee;

import com.github.games647.colorconsole.common.CommonFormatter;
import com.github.games647.colorconsole.common.LoggingLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.md_5.bungee.api.ProxyServer;

import static java.util.stream.Collectors.toSet;

public class ColorLogFormatter extends Formatter {

    private final Formatter oldFormatter;
    private final DateTimeFormatter date = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final EnumMap<LoggingLevel, String> levelColors;
    private final CommonFormatter formatter;

    public ColorLogFormatter(Formatter oldFormatter, EnumMap<LoggingLevel, String> levels,
                             Collection<String> hideMessages, boolean truncateColor) {
        this.oldFormatter = oldFormatter;
        this.levelColors = levels;
        this.formatter = new CommonFormatter(hideMessages, truncateColor);
    }

    @Override
    public String format(LogRecord record) {
        if (formatter.shouldIgnore(record.getMessage())) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();
        String message = oldFormatter.formatMessage(record);

        String levelColor = levelColors.getOrDefault(translateToLog4JName(record.getLevel()), "");
        formatted.append(levelColor);

        formatted.append(date.format(Instant.ofEpochMilli(record.getMillis())));
        formatted.append(" [");
        formatted.append(record.getLevel().getName());
        formatted.append("] ");

        formatted.append(formatter.getReset());

        formatted.append(formatter.colorizePluginTag(message));

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

    private LoggingLevel translateToLog4JName(Level level) {
        if (level == Level.SEVERE) {
            return LoggingLevel.ERROR;
        } else if (level == Level.WARNING) {
            return LoggingLevel.WARN;
        } else if (level == Level.INFO) {
            return LoggingLevel.INFO;
        } else if (level == Level.CONFIG) {
            return LoggingLevel.DEBUG;
        } else {
            return LoggingLevel.TRACE;
        }
    }

    private Set<String> loadPluginNames() {
        return ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .map(plugin -> plugin.getDescription().getName())
                .collect(toSet());
    }

    public void initPluginColors(Map<String, String> pluginColors, String def) {
        formatter.initPluginColors(loadPluginNames(), pluginColors, def);
    }
}
