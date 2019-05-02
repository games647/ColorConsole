package com.github.games647.colorconsole.sponge;

import com.github.games647.colorconsole.common.ColorAppender;
import com.github.games647.colorconsole.common.ConsoleConfig;
import com.github.games647.colorconsole.common.Log4JInstaller;
import com.github.games647.colorconsole.common.LoggingLevel;
import com.github.games647.colorconsole.common.PlatformPlugin;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION,
        url = PomData.URL, description = PomData.DESCRIPTION)
public class ColorConsoleSponge implements PlatformPlugin {

    //Console is maybe required too?
    private static final String TERMINAL_NAME = "MinecraftConsole";

    private final Path pluginFolder;
    private final Logger logger;

    private final Log4JInstaller installer = new Log4JInstaller();
    private Layout<? extends Serializable> oldLayout;

    @Inject
    public ColorConsoleSponge(Logger logger, @ConfigDir(sharedRoot = false) Path dataFolder) {
        this.pluginFolder = dataFolder;
        this.logger = logger;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        ConsoleConfig configuration;
        try {
            saveDefaultConfig();
            configuration = loadConfiguration();
        } catch (IOException ioEx) {
            logger.warn("Failed to load configuration file. Canceling plugin setup", ioEx);
            return;
        }

        installLogFormat(configuration);
    }

    @Override
    public void installLogFormat(ConsoleConfig configuration) {
        try {
            oldLayout = installer.installLog4JFormat(this, TERMINAL_NAME, configuration);
        } catch (ReflectiveOperationException reflectiveEx) {
            logger.error("Failed to install log format", reflectiveEx);
        }
    }

    @Override
    public void revertLogFormat() {
        try {
            installer.revertLog4JFormat(TERMINAL_NAME, oldLayout);
        } catch (ReflectiveOperationException reflectiveEx) {
            logger.warn("Cannot revert log format", reflectiveEx);
        }
    }

    @Override
    public ColorAppender createAppender(Appender oldAppender, Collection<String> hideMessages, boolean truncateCol) {
        return new SpongeAppender(oldAppender, hideMessages, truncateCol);
    }

    @Override
    public Path getPluginFolder() {
        return pluginFolder;
    }

    @Override
    public ConsoleConfig loadConfiguration() throws IOException {
        Path configPath = pluginFolder.resolve(CONFIG_NAME);
        YAMLConfigurationLoader configLoader = YAMLConfigurationLoader.builder().setPath(configPath).build();

        ConsoleConfig consoleConfig = new ConsoleConfig();
        ConfigurationNode rootNode = configLoader.load();
        consoleConfig.setLogFormat(rootNode.getNode("logFormat").getString());
        consoleConfig.setDateStyle(rootNode.getNode("dateStyle").getString());

        consoleConfig.getLevelColors().clear();
        if (rootNode.getNode("colorLoggingLevel").getBoolean()) {
            ConfigurationNode levelSection = rootNode.getNode("Level");
            for (LoggingLevel level : LoggingLevel.values()) {
                consoleConfig.getLevelColors().put(level, levelSection.getNode(level.name()).getString(""));
            }
        }

        consoleConfig.getPluginColors().clear();
        if (rootNode.getNode("colorPluginTag").getBoolean()) {
            ConfigurationNode pluginSection = rootNode.getNode("Plugin");
            consoleConfig.setDefaultPluginColor(pluginSection.getNode(ConsoleConfig.DEFAULT_PLUGIN_KEY).getString(""));
            for (Entry<Object, ? extends ConfigurationNode> pluginEntry : pluginSection.getChildrenMap().entrySet()) {
                consoleConfig.getPluginColors().put((String) pluginEntry.getKey(), pluginEntry.getValue().getString());
            }
        }

        consoleConfig.getHideMessages().clear();
        try {
            List<String> list = rootNode.getNode("hide-messages").getList(TypeToken.of(String.class));
            consoleConfig.getHideMessages().addAll(list);
        } catch (ObjectMappingException mappingException) {
            throw new IOException(mappingException);
        }

        consoleConfig.setTruncateColor(rootNode.getNode("truncateColor").getBoolean());
        return consoleConfig;
    }
}
