package com.github.games647.colorconsole.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fusesource.jansi.Ansi;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class CommonFormatterTest {

    private CommonFormatter formatter;

    @Before
    public void setUp() throws Exception {
        formatter = new CommonFormatter(Collections.singleton("ignore"), false);
    }

    @Test
    public void testShouldIgnore() {
        assertThat(formatter.shouldIgnore("123"), is(false));

        assertThat(formatter.shouldIgnore("ignore"), is(true));
        assertThat(formatter.shouldIgnore("start ignore end"), is(true));
    }

    @Test
    public void testColorizePluginTagPresent() {
        loadPluginColors();

        Ansi reset = Ansi.ansi().reset();
        String expected = "[" + Ansi.ansi().fgBlue() + "TestPlugin" + reset + "] msg" + reset;
        assertThat(formatter.colorizePluginTag("[TestPlugin] msg"), is(expected));
    }

    @Test
    public void testColorizePluginTagNotPresentRight() {
        loadPluginColors();

        // unmodified
        String msg = "[TestPlugin msg";
        assertThat(formatter.colorizePluginTag(msg), is(msg));
    }

    @Test
    public void testColorizePluginTagNotPresentLeft() {
        loadPluginColors();

        // unmodified
        String msg = "TestPlugin] msg";
        assertThat(formatter.colorizePluginTag(msg), is(msg));
    }

    @Test
    public void testColorizePluginTagWrongOrder() {
        loadPluginColors();

        // unmodified
        String msg = "]TestPlugin[ msg";
        assertThat(formatter.colorizePluginTag(msg), is(msg));
    }

    @Test
    public void testColorizeNameDefault() {
        loadPluginColors();

        Ansi reset = Ansi.ansi().reset();
        assertThat(formatter.colorizePluginName("None"), is(Ansi.ansi().fgRed() + "None" + reset));
    }

    @Test
    public void testColorizePluginName() {
        loadPluginColors();

        Ansi reset = Ansi.ansi().reset();
        assertThat(formatter.colorizePluginName("TestPlugin"), is(Ansi.ansi().fgBlue() + "TestPlugin" + reset));
    }

    private void loadPluginColors() {
        List<String> plugins = Arrays.asList("TestPlugin", "None");
        Map<String, String> map = Collections.singletonMap("TestPlugin", "blue");
        formatter.initPluginColors(plugins, map, "red");
    }
}
