package com.github.games647.colorconsole.bukkit;

import com.github.games647.colorconsole.common.ColorAppender;
import com.github.games647.colorconsole.common.ConsoleConfig;
import com.github.games647.colorconsole.common.Log4JInstaller;
import com.github.games647.colorconsole.common.LoggingLevel;
import com.github.games647.colorconsole.common.PlatformPlugin;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ColorConsoleBukkit extends JavaPlugin implements PlatformPlugin {

    private static final String TERMINAL_NAME = "TerminalConsole";
    private static final String CATSERVER_TERMINAL = "Console";


    private final Log4JInstaller installer = new Log4JInstaller();

    private Layout<? extends Serializable> oldLayout;

    @Override
    public void onLoad() {
        //try to run it as early as possible
        saveDefaultConfig();
        ConsoleConfig configuration = loadConfiguration();

        installLogFormat(configuration);
    }

    @Override
    public void onDisable() {
        revertLogFormat();
    }

    @Override
    public void installLogFormat(ConsoleConfig configuration) {
        try {
            oldLayout = installer.installLog4JFormat(this, getTerminalName(), configuration);
        } catch (ReflectiveOperationException reflectiveEx) {
            getLogger().log(Level.WARNING, "Failed to install log format", reflectiveEx);
        }
    }

    @Override
    public ColorAppender createAppender(Appender oldAppender, Collection<String> hideMessages, boolean truncateCol) {
        return new ColorPluginAppender(oldAppender, hideMessages, truncateCol);
    }

    @Override
    public void revertLogFormat() {
        try {
            installer.revertLog4JFormat(getTerminalName(), oldLayout);
        } catch (ReflectiveOperationException ex) {
            getLogger().log(Level.WARNING, "Cannot revert log format", ex);
        }
    }

    @Override
    public Path getPluginFolder() {
        return getDataFolder().toPath();
    }

    @Override
    public ConsoleConfig loadConfiguration() {
        FileConfiguration bukkitConfig = getConfig();

        ConsoleConfig consoleConfig = new ConsoleConfig();
        consoleConfig.setLogFormat(bukkitConfig.getString("logFormat"));
        consoleConfig.setDateStyle(bukkitConfig.getString("dateStyle"));

        consoleConfig.getLevelColors().clear();
        if (bukkitConfig.getBoolean("colorLoggingLevel")) {
            ConfigurationSection levelSection = bukkitConfig.getConfigurationSection("Level");
            for (LoggingLevel level : LoggingLevel.values()) {
                consoleConfig.getLevelColors().put(level, levelSection.getString(level.name(), ""));
            }
        }

        consoleConfig.getPluginColors().clear();
        if (bukkitConfig.getBoolean("colorPluginTag")) {
            ConfigurationSection pluginSection = bukkitConfig.getConfigurationSection("Plugin");
            consoleConfig.setDefaultPluginColor(pluginSection.getString(ConsoleConfig.DEFAULT_PLUGIN_KEY));
            for (String pluginKey : pluginSection.getKeys(false)) {
                consoleConfig.getPluginColors().put(pluginKey, pluginSection.getString(pluginKey));
            }
        }

        consoleConfig.getHideMessages().clear();
        consoleConfig.getHideMessages().addAll(bukkitConfig.getStringList("hide-messages"));

        consoleConfig.setTruncateColor(bukkitConfig.getBoolean("truncateColor"));
        return consoleConfig;
    }

    private String getTerminalName() {
        if (Bukkit.getVersion().contains("Cat")) {
            return CATSERVER_TERMINAL;
        }

        return TERMINAL_NAME;
    }
}
