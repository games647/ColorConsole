package com.github.games647.colorconsole.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiRenderer;

public class CommonFormatter {

    //copied from AnsiEscape in order to provide compatibility with older minecraft versions
    private static final String CSI = "\u001b[";
    private static final String SUFFIX = "m";
    private final String reset = Ansi.ansi().a(Ansi.Attribute.RESET).toString();

    private final Set<String> ignoreMessages;
    private final boolean colorizeTag;
    private final boolean truncateColor;
    private Map<String, String> pluginColors;

    public CommonFormatter(Collection<String> ignoreMessages, boolean colorizeTag, boolean truncateColor) {
        this.ignoreMessages = ImmutableSet.copyOf(ignoreMessages);
        this.colorizeTag = colorizeTag;
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

    public String colorizePluginTag(String message) {
        if (!message.contains("[") || !message.contains("]")) {
            return message;
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

        int startTag = newMessage.indexOf('[') + 1;
        int endTag = newMessage.indexOf(']', startTag);

        String pluginName = colorizePluginName(newMessage.substring(startTag, endTag));
        return '[' + pluginName + ']' + startingColorCode + newMessage.substring(endTag + 1);
    }

    public String colorizePluginName(String pluginName) {
        if (!colorizeTag) {
            return pluginName;
        }

        String pluginColor = pluginColors.getOrDefault(pluginName, "");
        return pluginColor + pluginName + reset;
    }

    public String format(String pluginFormat) {
        String[] formatParts = pluginFormat.split(" ");
        Ansi ansi = Ansi.ansi();
        for (String format : formatParts) {
            for (AnsiRenderer.Code ansiCode : AnsiRenderer.Code.values()) {
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

            for (Ansi.Color color : Ansi.Color.values()) {
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
