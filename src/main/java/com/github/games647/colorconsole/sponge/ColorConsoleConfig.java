package com.github.games647.colorconsole.sponge;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ninja.leaping.configurate.objectmapping.Setting;

public class ColorConsoleConfig {

    public ColorConsoleConfig() {
        this.pluginColors = Maps.newHashMap();
        this.pluginColors.put("ColorConsole", "yellow");

        this.levelColors = Maps.newHashMap();
        this.levelColors.put("FATAL", "red blink");
        this.levelColors.put("ERROR", "red");
        this.levelColors.put("WARN", "yellow bold");
        this.levelColors.put("INFO", "green");
        this.levelColors.put("DEBUG", "green bold");
        this.levelColors.put("TRACE", "blue");
    }

    @Setting(comment = "Should the plugin tag [PLUGIN_NAME] be highlighted")
    private final boolean colorPluginTag = true;

    @Setting(comment = "Should the log message be highlighted depending on the logging level")
    private final boolean colorLoggingLevel = true;

    @Setting(comment = "How the messages should be displayed\n"
            + '\n'
            + "Variables:\n"
            + "%thread - Thread name\n"
            + "%d{HH:mm:ss} - Timestamp\n"
            + "%msg - log message\n"
            + "%logger - logger name\n"
            + "%n - new line\n"
            + '\n'
            + "These variables try to get the origin. This is an expensive operation and may impact performance. "
            + "Use with caution.\n"
            + '\n'
            + "%class{precision} - Class name\n"
            + "%method - Method name\n"
            + "%line - Line number\n"
            + '\n'
            + "For more details visit: https://logging.apache.org/log4j/2.x/manual/layouts.html#Patterns")
    private final String logFormat = "[%d{HH:mm:ss} %level] [%logger{1}]: %msg%n";

    @Setting(comment = "Log Level Colors")
    private final Map<String, String> levelColors;

    @Setting(comment = "Plugin Colors or random")
    private final String defaultPluginColor = "blue";

    @Setting(comment = "Custom plugin colors")
    private final Map<String, String> pluginColors;

    @Setting(comment = "How should the time be highlighted\n" +
            "Like below it could also be default which means it's the default font color depending on " +
            "your terminal settings.")
    private final String dateStyle = "cyan";

    @Setting(comment = "Hides the log message if it contains one or more of the following texts\n"
            + "The texts are case-sensitive")
    private final List<String> hideMessages = Lists.newArrayList();

    @Setting(comment = "Removes color formatting if the complete message has color formatting")
    private boolean truncateColor;

    @Setting(comment = "Should the complete logging message be colored in the same style as the logging level " +
            "(like ColorConsole v1).\n" +
            "If not it's using the default color (like ColorConsole v2+)")
    private boolean colorMessage;

    public boolean isColorPluginTag() {
        return colorPluginTag;
    }

    public boolean isColorLoggingLevel() {
        return colorLoggingLevel;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public Map<String, String> getLevelColors() {
        return levelColors;
    }

    public String getDefaultPluginColor() {
        return defaultPluginColor;
    }

    public Map<String, String> getPluginColors() {
        return pluginColors;
    }

    public Collection<String> getHideMessages() {
        return ImmutableSet.copyOf(hideMessages);
    }

    public String getDateStyle() {
        return dateStyle;
    }

    public boolean isTruncateColor() {
        return truncateColor;
    }

    public boolean isColorMessage() {
        return colorMessage;
    }
}
