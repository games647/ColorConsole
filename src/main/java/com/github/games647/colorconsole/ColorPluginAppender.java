package com.github.games647.colorconsole;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

public class ColorPluginAppender extends AbstractAppender {

    private final Appender oldAppender;

    public ColorPluginAppender(Appender oldAppender) {
        super(oldAppender.getName(), null, oldAppender.getLayout());

        this.oldAppender = oldAppender;
    }

    @Override
    public void append(LogEvent event) {
        if (isStarted() && oldAppender.isStarted()) {
            Message newMessage = new SimpleMessage(colorizePluginTag(event.getMessage().getFormattedMessage()));

            LogEvent newEvent = new Log4jLogEvent(event.getLoggerName(), event.getMarker(), event.getFQCN()
                    , event.getLevel(), newMessage, event.getThrown(), event.getContextMap()
                    , event.getContextStack(), event.getThreadName(), event.getSource(), event.getMillis());
            oldAppender.append(newEvent);
        }
    }

    public Appender getOldAppender() {
        return oldAppender;
    }

    private String colorizePluginTag(String message) {
        if (!message.contains("[") || !message.contains("]")) {
            return message;
        }

        int start = message.indexOf('[') + 1;
        int end = message.indexOf(']', start);
        String prefix = Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLUE).toString();
        String resetSuffix = Ansi.ansi().a(Attribute.RESET).toString();
        return '[' + prefix + message.substring(start, end) + resetSuffix + message.substring(end);
    }
}
