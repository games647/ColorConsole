package com.github.games647.colorconsole.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.log.ColouredWriter;

public class ColorConsoleBungee extends Plugin {

    private Configuration configuration;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        File configFile = new File(getDataFolder(), "config.yml");
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ioEx) {
            getLogger().log(Level.SEVERE, "Unable to load configuration", ioEx);
            return;
        }

        //try to run it as early as possible
        installLogFormat();
    }

    @Override
    public void onDisable() {
        //restore the old format
        BungeeCord bungee = BungeeCord.getInstance();
        Logger bungeeLogger = bungee.getLogger();

        Handler[] handlers = bungeeLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ColouredWriter) {
                Formatter formatter = handler.getFormatter();
                if (formatter instanceof ColorLogFormatter) {
                    handler.setFormatter(((ColorLogFormatter) formatter).getOldFormatter());
                }
            }
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void installLogFormat() {
        BungeeCord bungee = BungeeCord.getInstance();
        Logger bungeeLogger = bungee.getLogger();

        Handler[] handlers = bungeeLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ColouredWriter) {
                Formatter oldFormatter = handler.getFormatter();

                ColorLogFormatter newFormatter = new ColorLogFormatter(this, oldFormatter);
                newFormatter.initPluginColors(getConfiguration().getString("PLUGIN"));
                handler.setFormatter(newFormatter);
            }
        }
    }

    private void saveDefaultConfig() {
        getDataFolder().mkdir();

        Path configFile = getDataFolder().toPath().resolve("config.yml");
        if (Files.notExists(configFile)) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile);
            } catch (IOException ioExc) {
                getLogger().log(Level.SEVERE, "Error saving default config", ioExc);
            }
        }
    }
}
