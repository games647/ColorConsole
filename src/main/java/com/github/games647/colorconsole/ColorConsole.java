package com.github.games647.colorconsole;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.plugin.java.JavaPlugin;

public class ColorConsole extends JavaPlugin {

    private Layout<? extends Serializable> oldLayout;

    @Override
    public void onLoad() {
        //try to run it as early as possible
        installLogFormat();
    }

    @Override
    public void onEnable() {
        installLogFormat();
    }

    @Override
    public void onDisable() {
        setLayout(oldLayout);
    }

    private void installLogFormat() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();

//        ConsoleAppender consoleAppender = (ConsoleAppender) conf.getAppenders().get("WINDOWS_COMPAT");
        Appender terminalAppender = conf.getAppenders().get("TerminalConsole");

        oldLayout = terminalAppender.getLayout();

        PatternLayout layout = PatternLayout
                .createLayout("[%highlight{%d{HH:mm:ss} %-5level]: %msg%n}{FATAL=red blink, ERROR=red, WARN=yellow bold, "
                        + "INFO=gray, DEBUG=green bold, TRACE=blue}", new DefaultConfiguration(), null
                        , Charset.defaultCharset().name(), "true");
        setLayout(layout);
    }

    private void setLayout(Layout<? extends Serializable> layout) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();

        Appender terminalAppender = conf.getAppenders().get("TerminalConsole");

        try {
            Field field = terminalAppender.getClass().getSuperclass().getDeclaredField("layout");
            field.setAccessible(true);
            field.set(terminalAppender, layout);
        } catch (Exception ex) {
            Logger.getLogger(ColorConsole.class.getName()).log(Level.SEVERE, "Failed to install log format", ex);
        }

//        conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(org.apache.logging.log4j.Level.ALL);
        ctx.updateLoggers(conf);
    }
}
