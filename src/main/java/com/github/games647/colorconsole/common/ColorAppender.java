package com.github.games647.colorconsole.common;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;

public abstract class ColorAppender extends AbstractAppender {

    private static final Method loggerClassGetter;
    private boolean disabled = loggerClassGetter == null;

    static {
        Method classGetter = null;
        for (Method method : LogEvent.class.getDeclaredMethods()) {
            String methodName = method.getName();
            if ("getLoggerFqcn".equalsIgnoreCase(methodName)
                    || "getFQCN".equalsIgnoreCase(methodName)) {
                classGetter = method;
                method.setAccessible(true);
                break;
            }
        }

        loggerClassGetter = classGetter;
    }

    private final Appender oldAppender;

    protected final CommonFormatter formatter;

    protected ColorAppender(Appender oldAppender, Collection<String> hideMessages
            , boolean colorizeTag, boolean truncateColor, Map<String, String> levelColors) {
        super(oldAppender.getName(), null, oldAppender.getLayout());

        this.oldAppender = oldAppender;
        this.formatter = new CommonFormatter(hideMessages, colorizeTag, truncateColor, levelColors);

    }

    public void initPluginColors(Map<String, String> configColors, String def) {
        formatter.initPluginColors(loadPluginNames(), configColors, def);
    }

    @Override
    public final void append(LogEvent logEvent) {
        if (oldAppender.isStarted()) {
            String oldMessage = logEvent.getMessage().getFormattedMessage();
            if (formatter.shouldIgnore(oldMessage)) {
                return;
            }

            oldAppender.append(onAppend(logEvent));
        }
    }

    protected abstract LogEvent onAppend(LogEvent logEvent);

    protected abstract Collection<String> loadPluginNames();

    protected LogEvent clone(LogEvent oldEvent, String loggerName, Message message) {
        String className = null;
        if (!disabled) {
            try {
                className = (String) loggerClassGetter.invoke(oldEvent);
            } catch (ReflectiveOperationException refEx) {
                //if this method cannot be found then the other methods wouldn't work neither
                disabled = true;
            }
        }

        return new Log4jLogEvent(loggerName, oldEvent.getMarker(), className
                , oldEvent.getLevel(), message, oldEvent.getThrown());
    }

    public Appender getOldAppender() {
        return oldAppender;
    }
}
