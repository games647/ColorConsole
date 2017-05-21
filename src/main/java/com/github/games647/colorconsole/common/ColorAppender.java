package com.github.games647.colorconsole.common;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;

public abstract class ColorAppender extends AbstractAppender {

    private final Appender oldAppender;

    protected final CommonFormatter formatter;

    protected ColorAppender(Appender oldAppender, Collection<String> hideMessages, boolean colorizeTag) {
        super(oldAppender.getName(), null, oldAppender.getLayout());

        this.oldAppender = oldAppender;
        this.formatter = new CommonFormatter(hideMessages, colorizeTag);
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
        return new Log4jLogEvent(loggerName, oldEvent.getMarker(), oldEvent.getFQCN()
                , oldEvent.getLevel(), message, oldEvent.getThrown()
                , oldEvent.getContextMap(), oldEvent.getContextStack()
                , oldEvent.getThreadName(), oldEvent.getSource(), oldEvent.getMillis());
    }

    public Appender getOldAppender() {
        return oldAppender;
    }
}
