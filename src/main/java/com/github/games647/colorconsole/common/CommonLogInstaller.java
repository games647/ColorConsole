package com.github.games647.colorconsole.common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

public class CommonLogInstaller {

    public static PatternLayout createLayout(String logFormat) throws ReflectiveOperationException {
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

    public static void installAppender(Appender colorAppender, String terminalName) {
        Logger rootLogger = (Logger) LogManager.getRootLogger();

        colorAppender.start();

        rootLogger.removeAppender(getTerminalAppender(terminalName));
        rootLogger.addAppender(colorAppender);
    }

    public static void setLayout(Layout<? extends Serializable> layout, Appender terminalAppender)
            throws ReflectiveOperationException {
        Field field = terminalAppender.getClass().getSuperclass().getDeclaredField("layout");
        field.setAccessible(true);
        field.set(terminalAppender, layout);
    }

    public static Appender getTerminalAppender(String terminalName) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();

        return conf.getAppenders().get(terminalName);
    }
}
