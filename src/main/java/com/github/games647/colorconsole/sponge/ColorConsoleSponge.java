package com.github.games647.colorconsole.sponge;

import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "colorconsole", name = "ColorConsole", version = "1.6"
        , url = "https://github.com/games647/ColorConsole/"
        , description = "Print colorful console messages depending on the logging level")
public class ColorConsoleSponge {

    private final Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File configFile;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private ObjectMapper<ColorConsoleConfig>.BoundInstance configMapper;
    private CommentedConfigurationNode rootNode;

    @Inject
    public ColorConsoleSponge(Logger logger) {
        this.logger = logger;
    }

    public ColorConsoleConfig getConfig() {
        return configMapper.getInstance();
    }

    public CommentedConfigurationNode getConfigRaw() {
        return rootNode;
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        logger.info("Setting up config");

        rootNode = configManager.createEmptyNode();
        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder()
                .setPath(configFile.toPath()).build();
        try {
            configMapper = ObjectMapper
                    .forClass(ColorConsoleConfig.class).bindToNew();

            rootNode = configManager.load();
            configMapper.populate(rootNode);

            //add and save missing values
            configMapper.serialize(rootNode);
            configLoader.save(rootNode);
        } catch (IOException | ObjectMappingException ioEx) {
            logger.error("Cannot save default config", ioEx);
            return;
        }

        installLogFormat();
        logger.warn("Test");
    }

    private void installLogFormat() {
        Appender terminalAppender = getTerminalAppender();
        
        String logFormat = configMapper.getInstance().getLogFormat();
        if (configMapper.getInstance().isColorLoggingLevel()) {
            logFormat = "%highlight{" + logFormat + "}{"
                    + "FATAL=" + configMapper.getInstance().getLevelColors().get("FATAL") + ", "
                    + "ERROR=" + configMapper.getInstance().getLevelColors().get("ERROR") + ", "
                    + "WARN=" + configMapper.getInstance().getLevelColors().get("WARN") + ", "
                    + "INFO=" + configMapper.getInstance().getLevelColors().get("INFO") + ", "
                    + "DEBUG=" + configMapper.getInstance().getLevelColors().get("DEBUG") + ", "
                    + "TRACE=" + configMapper.getInstance().getLevelColors().get("TRACE") + "}";
        }

        PatternLayout layout = PatternLayout
                .createLayout(logFormat, new DefaultConfiguration(), null, Charset.defaultCharset().name(), "true");
        setLayout(layout);

        if (configMapper.getInstance().isColorPluginTag()) {
            org.apache.logging.log4j.core.Logger rootLogger = ((org.apache.logging.log4j.core.Logger) LogManager
                    .getRootLogger());

            ColorPluginAppender pluginAppender = new ColorPluginAppender(terminalAppender, this);
            pluginAppender.start();

            rootLogger.removeAppender(terminalAppender);
            rootLogger.addAppender(pluginAppender);
        }
    }

    private void setLayout(Layout<? extends Serializable> layout) {
        Appender terminalAppender = getTerminalAppender();

        try {
            Field field = terminalAppender.getClass().getSuperclass().getDeclaredField("layout");
            field.setAccessible(true);
            field.set(terminalAppender, layout);
        } catch (Exception ex) {
            logger.error("Failed to install log format", ex);
        }
    }

    private Appender getTerminalAppender() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();

        return conf.getAppenders().get("FmlConsole");
    }
}
