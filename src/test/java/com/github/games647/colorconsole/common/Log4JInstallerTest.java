package com.github.games647.colorconsole.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class Log4JInstallerTest {

    private Log4JInstaller installer;

    @Before
    public void setUp() throws Exception {
        this.installer = new Log4JInstaller();
    }

    @Test
    public void testFormatDateVanilla() {
        String expected = "[%style{%d{HH:mm:ss}}{cyan} %level]: %msg%n";

        String configFormat = "[%d{HH:mm:ss} %level]: %msg%n";
        assertThat(installer.formatDate(configFormat, "cyan"), is(expected));
    }

    @Test
    public void testMappingLevels() {
        Map<LoggingLevel, String> levelColors = new HashMap<>();
        levelColors.put(LoggingLevel.FATAL, "red");
        levelColors.put(LoggingLevel.ERROR, "red");
        levelColors.put(LoggingLevel.WARN, "yellow");
        levelColors.put(LoggingLevel.INFO, "green");
        levelColors.put(LoggingLevel.DEBUG, "green");
        levelColors.put(LoggingLevel.TRACE, "blue");

        String configFormat = "[%d{HH:mm:ss} %level]: %msg%n";
        String expected = "[%d{HH:mm:ss} %highlight{%level}{" +
                "WARN=yellow, ERROR=red, DEBUG=green, FATAL=red, TRACE=blue, INFO=green" +
                "}]: %msg%n";
        assertThat(installer.mapLoggingLevels(configFormat, levelColors), is(expected));
    }

    @Test
    public void testMinecrellDetection() {
        PatternLayout crellPattern = PatternLayout.newBuilder()
                .withPattern("%highlightError{[%d{HH:mm:ss} %level]: [%logger] %minecraftFormatting{%msg}%n%xEx}")
                .build();
        String crellAppender = "net.minecrell.terminalconsole.TerminalConsoleAppender";
        assertThat(installer.isMinecrellFormatted(crellPattern, ""), is(true));
        assertThat(installer.isMinecrellFormatted(PatternLayout.createDefaultLayout(), crellAppender), is(true));
    }

    @Test
    public void testVanillaDetection() {
        PatternLayout vanillaPattern = PatternLayout.newBuilder()
                .withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n")
                .build();
        String vanillaAppender = "org.apache.logging.log4j.core.appender.ConsoleAppender";
        assertThat(installer.isMinecrellFormatted(vanillaPattern, "vanillaAppender"), is(false));
    }
}
