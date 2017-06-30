package com.github.games647.colorconsole.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiRenderer.Code;

public class CommonFormatter {

    //copied from AnsiEscape in order to provide compatibility with older minecraft versions
    private static final String CSI = "\u001b[";
    private static final String SUFFIX = "m";
    private final String reset = Ansi.ansi().a(Ansi.Attribute.RESET).toString();

    private final Set<String> ignoreMessages;
    private final boolean colorizeTag;
    private final boolean truncateColor;
    private Map<String, String> pluginColors;
    private Map<String, String> levelColors;

    public CommonFormatter(Collection<String> ignoreMessages, boolean colorizeTag, boolean truncateColor
            , Map<String, String> levelColors) {
        this.ignoreMessages = ImmutableSet.copyOf(ignoreMessages);

        this.colorizeTag = colorizeTag;
        this.truncateColor = truncateColor;

        Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<String, String> entry : levelColors.entrySet()) {
            if (entry.getKey().equals("INFO")) {
                continue;
            }

            builder.put(entry.getKey(), format(entry.getValue()));
        }

        this.levelColors = builder.build();
    }

    public boolean shouldIgnore(String message) {
        if (message == null) {
            return false;
        }

        for (String ignore : ignoreMessages) {
            if (message.contains(ignore)) {
                return true;
            }
        }

        return false;
    }

    public void initPluginColors(Collection<String> plugins, Map<String, String> configColors, String def) {
        Random random = new Random();
        Color[] colors = Color.values();
        //remove black, because it's often hard to read
        colors = Arrays.copyOfRange(colors, 1, colors.length);

        ImmutableMap.Builder<String, String> colorBuilder = ImmutableMap.builder();
        for (String plugin : plugins) {
            String styleCode = configColors.getOrDefault(plugin, def);
            if (styleCode.equalsIgnoreCase("random")) {
                //ignore default
                styleCode = colors[random.nextInt(colors.length - 1)].name();
            }

            colorBuilder.put(plugin, format(styleCode));
        }

        this.pluginColors = colorBuilder.build();
    }

    public String colorizePluginTag(String message, String level) {
        if (!message.contains("[") || !message.contains("]")) {
            return levelColors.getOrDefault(level, "") + message + reset;
        }

        String newMessage = message;

        String startingColorCode = "";
        if (message.startsWith(CSI)) {
            int endColor = message.indexOf(SUFFIX);

            newMessage = message.substring(endColor + 1, message.length());
            if (!truncateColor) {
                startingColorCode = message.substring(0, endColor + 1);
            }
        }

        if (!newMessage.startsWith("[")) {
            return levelColors.getOrDefault(level, "") + message + reset;
        }

        int startTag = newMessage.indexOf('[') + 1;
        int endTag = newMessage.indexOf(']', startTag);

        String pluginName = colorizePluginName(newMessage.substring(startTag, endTag));
        return '[' + pluginName + ']' + startingColorCode
                + levelColors.getOrDefault(level, "") + newMessage.substring(endTag + 1) + reset;
    }

    public String colorizePluginName(String pluginName) {
        if (!colorizeTag) {
            return pluginName;
        }

        String pluginColor = pluginColors.getOrDefault(pluginName, "");
        return pluginColor + pluginName + reset;
    }

    public String format(String keyCode) {
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
                continue;
            }

            for (Color color : Color.values()) {
                if (format.equalsIgnoreCase(color.name())) {
                    ansi.fg(color);
                    break;
                }
            }
        }

        return ansi.toString();
    }

    public String getReset() {
        return reset;
    }
}
