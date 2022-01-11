package com.paperspacecraft.aem.buildmonitor.util;

import org.apache.maven.plugin.testing.SilentLog;

public class DummyLog extends SilentLog {
    private static final StringBuilder messages = new StringBuilder();

    @Override
    public void info(String message) {
        info((CharSequence) message);
    }

    @Override
    public void info(CharSequence content) {
        messages.append(content).append("\n");
    }

    @Override
    public void error(String message) {
        error((CharSequence) message);
    }

    @Override
    public void error(CharSequence content) {
        messages.append(content).append("\n");
    }

    public String getMessages() {
        return messages.toString();
    }
}
