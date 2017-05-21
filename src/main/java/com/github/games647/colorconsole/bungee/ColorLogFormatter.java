package com.github.games647.colorconsole.bungee;

import com.github.games647.colorconsole.common.CommonFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ProxyServer;

public class ColorLogFormatter extends Formatter {

    private final ColorConsoleBungee plugin;
    private final Formatter oldFormatter;

    private final DateFormat date = new SimpleDateFormat("HH:mm:ss");

    private final CommonFormatter formatter;

    public ColorLogFormatter(ColorConsoleBungee plugin, Formatter oldFormatter) {
        this.plugin = plugin;
        this.oldFormatter = oldFormatter;

        List<String> ignoreMessages = plugin.getConfiguration().getStringList("hide-messages");
        boolean colorizeTag = plugin.getConfiguration().getBoolean("colorPluginTag");
        this.formatter = new CommonFormatter(ignoreMessages, colorizeTag);
    }

    @Override
    public String format(LogRecord record) {
        if (formatter.shouldIgnore(record.getMessage())) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();
        String message = oldFormatter.formatMessage(record);

        String levelColor = "";
        if (plugin.getConfiguration().getBoolean("colorLoggingLevel")) {
            String log4JName = translateToLog4JName(record.getLevel());
            levelColor = formatter.format(plugin.getConfiguration().getString(log4JName));
        }

        formatted.append(levelColor);

        formatted.append(this.date.format(record.getMillis()));
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

    private Set<String> loadPluginNames() {
        return ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .map(plugin -> plugin.getDescription().getName())
                .collect(Collectors.toSet());
    }

    public void initPluginColors(String def) {
        Set<String> plugins = loadPluginNames();
        Map<String, String> pluginColors = new HashMap<>();
        for (String pluginName : plugins) {
            String color = plugin.getConfiguration().getString("P-" + pluginName);
            if (color == null) {
                continue;
            }

            pluginColors.put(pluginName, color);
        }

        formatter.initPluginColors(plugins, pluginColors, def);
    }
}
