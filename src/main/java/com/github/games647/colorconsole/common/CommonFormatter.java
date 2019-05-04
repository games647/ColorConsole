package com.github.games647.colorconsole.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import io.netty.util.internal.ThreadLocalRandom;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiRenderer.Code;

public class CommonFormatter {

    //copied from AnsiEscape in order to provide compatibility with older Minecraft versions
    private static final String CSI = "\u001b[";
    private static final char SUFFIX = 'm';

    private static final Pattern TAG_PATTERN = Pattern.compile("^\\[.+\\].*$");

    private final String reset = Ansi.ansi().a(Attribute.RESET).toString();

    private final Set<String> ignoreMessages;
    private final boolean truncateColor;
    private Map<String, String> pluginColors;

    public CommonFormatter(Collection<String> ignoreMessages, boolean truncateColor) {
        this.ignoreMessages = ImmutableSet.copyOf(ignoreMessages);
        this.truncateColor = truncateColor;
    }

    public boolean shouldIgnore(String message) {
        for (String ignore : ignoreMessages) {
            if (message.contains(ignore)) {
                return true;
            }
        }

        return false;
    }

    public void initPluginColors(Iterable<String> plugins, Map<String, String> configColors, String def) {
        Color[] colors = Color.values();
        // remove black, because it's often hard to read
        colors = Arrays.copyOfRange(colors, 1, colors.length);

        Builder<String, String> colorBuilder = ImmutableMap.builder();
        for (String plugin : plugins) {
            String styleCode = configColors.getOrDefault(plugin, def);
            if ("random".equalsIgnoreCase(styleCode)) {
                //ignore default
                styleCode = colors[ThreadLocalRandom.current().nextInt(colors.length - 1)].name();
            }

            colorBuilder.put(plugin, format(styleCode));
        }

        this.pluginColors = colorBuilder.build();
    }

    public String colorizePluginTag(String message) {
        String newMessage = message;
        if (!TAG_PATTERN.matcher(message).matches()) {
            return newMessage;
        }

        String startingColorCode = "";
        if (message.startsWith(CSI)) {
            int endColor = message.indexOf(SUFFIX);

            newMessage = message.substring(endColor + 1);
            if (!truncateColor) {
                startingColorCode = message.substring(0, endColor + 1);
            }
        }

        int startTag = newMessage.indexOf('[') + 1;
        int endTag = newMessage.indexOf(']', startTag);

        String pluginName = colorizePluginName(newMessage.substring(startTag, endTag));
        return '[' + pluginName + ']' + startingColorCode + newMessage.substring(endTag + 1) + reset;
    }

    public String colorizePluginName(String pluginName) {
        String pluginColor = pluginColors.getOrDefault(pluginName, "");
        return pluginColor + pluginName + reset;
    }

    private String format(String keyCode) {
        String[] formatParts = keyCode.split(" ");
        Ansi ansi = Ansi.ansi();
        for (String format : formatParts) {
            for (Code ansiCode : Code.values()) {
                if (ansiCode.name().equalsIgnoreCase(format)) {
                    if (ansiCode.isAttribute()) {
                        ansi.a(ansiCode.getAttribute());
                    } else if (ansiCode.isBackground()) {
                        ansi.bg(ansiCode.getColor());
                    } else {
                        ansi.fg(ansiCode.getColor());
                    }
                }
            }

            if ("blink".equalsIgnoreCase(format)) {
                ansi.a(Attribute.BLINK_SLOW);
                continue;
            }

            if ("strikethrough".equalsIgnoreCase(format)) {
                ansi.a(Attribute.STRIKETHROUGH_ON);
                continue;
            }

            if ("hidden".equalsIgnoreCase(format)) {
                ansi.a(Attribute.CONCEAL_OFF);
                continue;
            }

            if ("dim".equalsIgnoreCase(format)) {
                ansi.a(Attribute.INTENSITY_FAINT);
                continue;
            }

            if ("reverse".equalsIgnoreCase(format)) {
                ansi.a(Attribute.NEGATIVE_ON);
            }
        }

        return ansi.toString();
    }

    public String getReset() {
        return reset;
    }
}
