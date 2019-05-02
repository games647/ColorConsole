package com.github.games647.colorconsole.bungee;

import com.github.games647.colorconsole.common.ColorAppender;
import com.github.games647.colorconsole.common.ConsoleConfig;
import com.github.games647.colorconsole.common.Log4JInstaller;
import com.github.games647.colorconsole.common.LoggingLevel;
import com.github.games647.colorconsole.common.PlatformPlugin;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;

public class ColorConsoleBungee extends Plugin implements PlatformPlugin {

    private static final String TERMINAL_NAME = "TerminalConsole";

    private final Log4JInstaller installer = new Log4JInstaller();
    private Layout<? extends Serializable> oldLayout;

    @Override
    public void onEnable() {
        ConsoleConfig configuration;
        try {
            saveDefaultConfig();
            configuration = loadConfiguration();
        } catch (IOException ioEx) {
            getLogger().log(Level.SEVERE, "Unable to load configuration", ioEx);
            return;
        }

        installLogFormat(configuration);
    }

    @Override
    public void onDisable() {
        revertLogFormat();
    }

    @Override
    public void installLogFormat(ConsoleConfig config) {
        if (isWaterfallLog4J()) {
            try {
                oldLayout = installer.installLog4JFormat(this, TERMINAL_NAME, config);
            } catch (ReflectiveOperationException reflectiveEx) {
                getLogger().log(Level.WARNING, "Cannot install log format", reflectiveEx);
            }
        } else {
            getLogger().info("Waterfall Log4J not detected. Falling back to vanilla logging");
            ProxyServer bungee = ProxyServer.getInstance();
            Logger bungeeLogger = bungee.getLogger();

            Handler[] handlers = bungeeLogger.getHandlers();
            for (Handler handler : handlers) {
                Formatter oldFormatter = handler.getFormatter();

                EnumMap<LoggingLevel, String> levelColors = config.getLevelColors();
                Collection<String> hideMessages = config.getHideMessages();
                boolean truncateCol = config.isTruncateColor();
                ColorLogFormatter newForm = new ColorLogFormatter(oldFormatter, levelColors, hideMessages, truncateCol);

                newForm.initPluginColors(config.getPluginColors(), config.getDefaultPluginColor());
                handler.setFormatter(newForm);
            }
        }
    }

    @Override
    public ColorAppender createAppender(Appender oldAppender, Collection<String> hideMessages, boolean truncateCol) {
        return new ColorPluginAppender(oldAppender, hideMessages, truncateCol);
    }

    @Override
    public void revertLogFormat() {
        if (isWaterfallLog4J()) {
            try {
                installer.revertLog4JFormat(TERMINAL_NAME, oldLayout);
            } catch (ReflectiveOperationException reflectiveEx) {
                getLogger().log(Level.WARNING, "Cannot revert logging format", reflectiveEx);
            }
        } else {
            ProxyServer bungee = ProxyServer.getInstance();
            Logger bungeeLogger = bungee.getLogger();

            Handler[] handlers = bungeeLogger.getHandlers();
            for (Handler handler : handlers) {
                Formatter formatter = handler.getFormatter();
                if (formatter instanceof ColorLogFormatter) {
                    handler.setFormatter(((ColorLogFormatter) formatter).getOldFormatter());
                }
            }
        }
    }

    @Override
    public Path getPluginFolder() {
        return getDataFolder().toPath();
    }

    @Override
    public ConsoleConfig loadConfiguration() throws IOException {
        Path configPath = getPluginFolder().resolve(CONFIG_NAME);

        ConfigurationProvider yamlProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        Configuration bungeeConfig = yamlProvider.load(configPath.toFile());

        ConsoleConfig consoleConfig = new ConsoleConfig();
        consoleConfig.setLogFormat(bungeeConfig.getString("logFormat"));
        consoleConfig.setDateStyle(bungeeConfig.getString("dateStyle"));

        consoleConfig.getLevelColors().clear();
        if (bungeeConfig.getBoolean("colorLoggingLevel")) {
            Configuration levelSection = bungeeConfig.getSection("Level");
            for (LoggingLevel level : LoggingLevel.values()) {
                consoleConfig.getLevelColors().put(level, levelSection.getString(level.name(), ""));
            }
        }

        consoleConfig.getPluginColors().clear();
        if (bungeeConfig.getBoolean("colorPluginTag")) {
            Configuration pluginSection = bungeeConfig.getSection("Plugin");
            consoleConfig.setDefaultPluginColor(pluginSection.getString(ConsoleConfig.DEFAULT_PLUGIN_KEY));
            for (String pluginKey : pluginSection.getKeys()) {
                consoleConfig.getPluginColors().put(pluginKey, pluginSection.getString(pluginKey));
            }
        }

        consoleConfig.getHideMessages().clear();
        consoleConfig.getHideMessages().addAll(bungeeConfig.getStringList("hide-messages"));

        consoleConfig.setTruncateColor(bungeeConfig.getBoolean("truncateColor"));
        return consoleConfig;
    }

    private boolean isWaterfallLog4J() {
        try {
            Class.forName("io.github.waterfallmc.waterfall.log4j.WaterfallLogger");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
