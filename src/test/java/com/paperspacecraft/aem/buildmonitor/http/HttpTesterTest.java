package com.paperspacecraft.aem.buildmonitor.http;

import com.paperspacecraft.aem.buildmonitor.util.DummyContentProvider;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.SilentLog;
import org.junit.Assert;
import org.junit.Test;

public class HttpTesterTest {

    private static final ContentProvider HTML_CONTENT_PROVIDER = new DummyContentProvider();
    private static final Log SILENT_LOG = new SilentLog();

    @Test
    public void shouldCheckHttpResponse() {
        Assert.assertTrue(getHtmlMatchingTester("body .root .column .cell").test());
        Assert.assertFalse(getHtmlMatchingTester("body .root .column2 .cell").test());
        Assert.assertTrue(getHtmlMatchingTester("body .root .column .cell | Hello world").test());
        Assert.assertFalse(getHtmlMatchingTester("body .root .column .cell | Goodbye world").test());
    }

    @Test
    public void shouldCheckTextResponse() {
        Assert.assertTrue(getTextMatchingTester("Hello world").test());
        Assert.assertTrue(getTextMatchingTester("Sample Response").test());
        Assert.assertFalse(getTextMatchingTester("Goodbye world").test());
    }

    private static HttpTester getHtmlMatchingTester(String query) {
        return HttpTester
                .builder()
                .contentProvider(HTML_CONTENT_PROVIDER)
                .logger(SILENT_LOG)
                .mustContainHtml(query)
                .build();
    }

    private static HttpTester getTextMatchingTester(String query) {
        return HttpTester
                .builder()
                .contentProvider(HTML_CONTENT_PROVIDER)
                .logger(SILENT_LOG)
                .mustContainText(query)
                .build();
    }
}
