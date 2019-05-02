package com.github.games647.colorconsole.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleConfig {

    public static String DEFAULT_PLUGIN_KEY = "Default";

    private String logFormat;
    private String dateStyle;

    private final EnumMap<LoggingLevel, String> levelColors = new EnumMap<>(LoggingLevel.class);

    private String defaultPluginColor;
    private final Map<String, String> pluginColors = new HashMap<>();

    private final List<String> hideMessages = new ArrayList<>();
    private boolean truncateColor;

    public String getLogFormat() {
        return logFormat;
    }

    public EnumMap<LoggingLevel, String> getLevelColors() {
        return levelColors;
    }

    public String getDefaultPluginColor() {
        return defaultPluginColor;
    }

    public Map<String, String> getPluginColors() {
        return pluginColors;
    }

    public Collection<String> getHideMessages() {
        return hideMessages;
    }

    public String getDateStyle() {
        return dateStyle;
    }

    public boolean isTruncateColor() {
        return truncateColor;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    public void setDateStyle(String dateStyle) {
        this.dateStyle = dateStyle;
    }

    public void setDefaultPluginColor(String defaultPluginColor) {
        this.defaultPluginColor = defaultPluginColor;
    }

    public void setTruncateColor(boolean truncateColor) {
        this.truncateColor = truncateColor;
    }
}
