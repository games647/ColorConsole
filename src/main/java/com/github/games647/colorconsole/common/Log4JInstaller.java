package com.github.games647.colorconsole.common;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

public class Log4JInstaller {

    public PatternLayout createLayout(String logFormat) throws ReflectiveOperationException {
        try {
            Method builder = PatternLayout.class
                    .getDeclaredMethod("createLayout", String.class, Configuration.class, RegexReplacement.class
                            , String.class, String.class);

            return (PatternLayout) builder.invoke(null, logFormat, new DefaultConfiguration(), null
                    , Charset.defaultCharset().name(), "true");
        } catch (NoSuchMethodException methodEx) {
            return PatternLayout.newBuilder()
                    .withCharset(Charset.defaultCharset())
                    .withPattern(logFormat)
                    .withConfiguration(new DefaultConfiguration())
                    .withAlwaysWriteExceptions(true)
                    .build();
        }
    }

    public void installAppender(Appender colorAppender, String terminalName) {
        Logger rootLogger = (Logger) LogManager.getRootLogger();

        colorAppender.start();

        rootLogger.removeAppender(getTerminalAppender(terminalName));
        rootLogger.addAppender(colorAppender);
    }

    public void setLayout(Layout<? extends Serializable> layout, Appender terminalAppender)
            throws ReflectiveOperationException {
        Field field = terminalAppender.getClass().getSuperclass().getDeclaredField("layout");
        field.setAccessible(true);
        field.set(terminalAppender, layout);
    }

    public Layout<? extends Serializable> installLog4JFormat(PlatformPlugin pl,
                                                             String terminalName, ConsoleConfig config)
            throws ReflectiveOperationException {
        Appender terminalAppender = getTerminalAppender(terminalName);
        Layout<? extends Serializable> oldLayout = terminalAppender.getLayout();

        String logFormat = config.getLogFormat();
        String appenderClass = terminalAppender.getClass().getName();
        if (isMinecrellFormatted(oldLayout, appenderClass)) {
            logFormat = logFormat.replace("%msg", "%minecraftFormatting{%msg}");
        }

        logFormat = mapLoggingLevels(logFormat, config.getLevelColors());
        logFormat = formatDate(logFormat, config.getDateStyle());

        PatternLayout layout = createLayout(logFormat);
        setLayout(layout, terminalAppender);

        Collection<String> hideMessages = config.getHideMessages();
        boolean truncateColor = config.isTruncateColor();
        ColorAppender appender = pl.createAppender(terminalAppender, hideMessages, truncateColor);
        appender.initPluginColors(config.getPluginColors(), config.getDefaultPluginColor());

        installAppender(appender, terminalName);
        return oldLayout;
    }

    public void revertLog4JFormat(String terminalName, Layout<? extends Serializable> oldLayout)
            throws ReflectiveOperationException {
        Appender terminalAppender = getTerminalAppender(terminalName);

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        ColorAppender colorPluginAppender = null;
        for (Appender value : rootLogger.getAppenders().values()) {
            if (value instanceof ColorAppender) {
                colorPluginAppender = (ColorAppender) value;
                break;
            }
        }

        if (colorPluginAppender != null) {
            rootLogger.removeAppender(terminalAppender);
            rootLogger.addAppender(colorPluginAppender.getOldAppender());
        }

        setLayout(oldLayout, terminalAppender);
    }

    public Appender getTerminalAppender(String terminalName) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();
        return conf.getAppender(terminalName);
    }

    @VisibleForTesting
    protected boolean isMinecrellFormatted(Layout<? extends Serializable> oldLayout, String appenderClass) {
        return oldLayout.toString().contains("minecraftFormatting") || appenderClass.contains("minecrell");
    }

    @VisibleForTesting
    protected String formatDate(String logFormat, String dateStyle) {
        return logFormat.replaceFirst("(%d)\\{.*?}", "%style{$0}{" + dateStyle + '}');
    }

    @VisibleForTesting
    protected String mapLoggingLevels(String logFormat, Map<LoggingLevel, String> levelColors) {
        Map<LoggingLevel, String> sortedColors = new EnumMap<>(LoggingLevel.class);
        sortedColors.putAll(levelColors);

        String levelFormat = Joiner.on(", ").withKeyValueSeparator('=').join(sortedColors);
        return logFormat.replace("%level", "%highlight{%level}{" + levelFormat + '}');
    }
}
