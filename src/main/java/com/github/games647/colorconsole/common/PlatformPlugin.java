package com.github.games647.colorconsole.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.logging.log4j.core.Appender;

public interface PlatformPlugin {

    String CONFIG_NAME = "config.yml";

    void installLogFormat(ConsoleConfig configuration);

    ColorAppender createAppender(Appender oldAppender, Collection<String> hideMessages, boolean truncateCol);

    //restore the old format
    void revertLogFormat();

    Path getPluginFolder();

    ConsoleConfig loadConfiguration() throws IOException;

    default void saveDefaultConfig() throws IOException {
        Path dataFolder = getPluginFolder();
        if (Files.notExists(dataFolder)) {
            Files.createDirectories(dataFolder);
        }

        Path configFile = dataFolder.resolve(CONFIG_NAME);
        if (Files.notExists(configFile)) {
            try (InputStream defaultStream = getClass().getClassLoader().getResourceAsStream(CONFIG_NAME)) {
                if (defaultStream == null) {
                    return;
                }

                Files.copy(defaultStream, configFile);
            }
        }
    }
}
