package com.github.games647.colorconsole.bukkit;

import com.github.games647.colorconsole.common.CommonLogInstaller;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.plugin.java.JavaPlugin;

public class ColorConsoleBukkit extends JavaPlugin {

    private static final String TERMINAL_NAME = "TerminalConsole";

    private Layout<? extends Serializable> oldLayout;

    @Override
    public void onLoad() {
        Map<String, String> levelColors = Maps.newHashMap();
        levelColors.put("FATAL", getConfig().getString("FATAL"));
        levelColors.put("ERROR", getConfig().getString("ERROR"));
        levelColors.put("WARN", getConfig().getString("WARN"));
        levelColors.put("DEBUG", getConfig().getString("DEBUG"));
        levelColors.put("TRACE", getConfig().getString("TRACE"));

        //try to run it as early as possible
        installLogFormat(levelColors);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        //restore the old format
        Appender terminalAppender = CommonLogInstaller.getTerminalAppender(TERMINAL_NAME);
        Logger rootLogger = ((Logger) LogManager.getRootLogger());

        ColorPluginAppender colorPluginAppender = null;
        for (Appender value : rootLogger.getAppenders().values()) {
            if (value instanceof ColorPluginAppender) {
                colorPluginAppender = (ColorPluginAppender) value;
                break;
            }
        }

        if (colorPluginAppender != null) {
            rootLogger.removeAppender(terminalAppender);
            rootLogger.addAppender(colorPluginAppender.getOldAppender());
        }

        try {
            CommonLogInstaller.setLayout(oldLayout, terminalAppender);
        } catch (ReflectiveOperationException ex) {
            getLogger().log(Level.WARNING, "Cannot revert log format", ex);
        }
    }

    private void installLogFormat(Map<String, String> levelColors) {
        Appender terminalAppender = CommonLogInstaller.getTerminalAppender(TERMINAL_NAME);

        oldLayout = terminalAppender.getLayout();
        String logFormat = getConfig().getString("logFormat");
        String appenderClass = terminalAppender.getClass().getName();
        if (oldLayout.toString().contains("minecraftFormatting") || appenderClass.contains("minecrell")) {
            logFormat = logFormat.replace("%msg", "%minecraftFormatting{%msg}");
        }

        if (getConfig().getBoolean("colorLoggingLevel")) {
            logFormat = logFormat.replace("%level",  "%highlight{%level}{"
                    + "FATAL=" + getConfig().getString("FATAL") + ", "
                    + "ERROR=" + getConfig().getString("ERROR") + ", "
                    + "WARN=" + getConfig().getString("WARN") + ", "
                    + "INFO=" + getConfig().getString("INFO") + ", "
                    + "DEBUG=" + getConfig().getString("DEBUG") + ", "
                    + "TRACE=" + getConfig().getString("TRACE") + '}');
        }

        String dateStyle = getConfig().getString("dateStyle");
        logFormat = logFormat.replaceFirst("(%d)\\{.{1,}\\}", "%style{$0}{" + dateStyle + '}');
        try {
            PatternLayout layout = CommonLogInstaller.createLayout(logFormat);
            CommonLogInstaller.setLayout(layout, terminalAppender);
        } catch (ReflectiveOperationException ex) {
            getLogger().log(Level.WARNING, "Cannot install log format", ex);
        }

        ColorPluginAppender pluginAppender = new ColorPluginAppender(terminalAppender, getConfig(), levelColors);
        Map<String, String> colors = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : getConfig().getValues(false).entrySet()) {
            if (!entry.getKey().startsWith("P-")) {
                continue;
            }

            colors.put(entry.getKey().replace("P-", ""), (String) entry.getValue());
        }

        pluginAppender.initPluginColors(colors, getConfig().getString("PLUGIN"));
        CommonLogInstaller.installAppender(pluginAppender, TERMINAL_NAME);
    }
}
