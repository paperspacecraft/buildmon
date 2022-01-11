package com.paperspacecraft.aem.buildmonitor.util;

import com.paperspacecraft.aem.buildmonitor.http.ContentProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DummyContentProvider implements ContentProvider {
    @Override
    public String getEndpoint() {
        return "http://sample.com/response";
    }

    @Override
    public String getContent() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("com/paperspacecraft/aem/buildmonitor/http/response.html");
        if (stream == null) {
            return StringUtils.EMPTY;
        }
        try {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }
    }
}
