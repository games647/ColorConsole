package com.github.games647.colorconsole.sponge;

import com.github.games647.colorconsole.common.CommonLogInstaller;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION
        , url = PomData.URL, description = PomData.DESCRIPTION)
public class ColorConsoleSponge {

    private static final String TERMINAL_NAME = "MinecraftConsole";

    private final Logger logger;

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

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        logger.info("Setting up config");

        rootNode = configManager.createEmptyNode();
        try {
            configMapper = ObjectMapper.forClass(ColorConsoleConfig.class).bindToNew();

            rootNode = configManager.load();
            configMapper.populate(rootNode);

            //add and save missing values
            configMapper.serialize(rootNode);
            configManager.save(rootNode);
        } catch (IOException | ObjectMappingException ioEx) {
            logger.error("Cannot save default config", ioEx);
            return;
        }

        installLogFormat();
    }

    private void installLogFormat() {
        Appender terminalAppender = CommonLogInstaller.getTerminalAppender(TERMINAL_NAME);

        Layout<? extends Serializable> oldLayout = terminalAppender.getLayout();
        String logFormat = configMapper.getInstance().getLogFormat();
        String appenderClass = terminalAppender.getClass().getName();
        if (oldLayout.toString().contains("minecraftFormatting") || appenderClass.contains("minecrell")) {
            logFormat = logFormat.replace("%msg", "%minecraftFormatting{%msg}");
        }

        if (configMapper.getInstance().isColorLoggingLevel()) {
            logFormat = logFormat.replace("%level",  "%highlight{%level}{"
                    + "FATAL=" + configMapper.getInstance().getLevelColors().get("FATAL") + ", "
                    + "ERROR=" + configMapper.getInstance().getLevelColors().get("ERROR") + ", "
                    + "WARN=" + configMapper.getInstance().getLevelColors().get("WARN") + ", "
                    + "INFO=" + configMapper.getInstance().getLevelColors().get("INFO") + ", "
                    + "DEBUG=" + configMapper.getInstance().getLevelColors().get("DEBUG") + ", "
                    + "TRACE=" + configMapper.getInstance().getLevelColors().get("TRACE") + '}');
        }

        String dateStyle = configMapper.getInstance().getDateStyle();
        logFormat = logFormat.replace("%d{HH:mm:ss}", "%style{" + "%d{HH:mm:ss}" + "}{" + dateStyle + '}');

        try {
            PatternLayout layout = CommonLogInstaller.createLayout(logFormat);
            CommonLogInstaller.setLayout(layout, terminalAppender);
        } catch (ReflectiveOperationException ex) {
            logger.warn("Cannot install log format", ex);
        }

        ColorPluginAppender pluginAppender = new ColorPluginAppender(terminalAppender, getConfig());
        pluginAppender.initPluginColors(getConfig().getPluginColors(), getConfig().getDefaultPluginColor());

        CommonLogInstaller.installAppender(pluginAppender, TERMINAL_NAME);
        CommonLogInstaller.installAppender(pluginAppender, "Console");
    }
}
