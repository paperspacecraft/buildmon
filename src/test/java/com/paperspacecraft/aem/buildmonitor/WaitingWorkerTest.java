package com.paperspacecraft.aem.buildmonitor;

import com.paperspacecraft.aem.buildmonitor.http.ContentProvider;
import com.paperspacecraft.aem.buildmonitor.util.DummyContentProvider;
import com.paperspacecraft.aem.buildmonitor.http.HttpTester;
import com.paperspacecraft.aem.buildmonitor.util.DummyLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.SilentLog;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class WaitingWorkerTest {

    private static final Log SILENT_LOG = new SilentLog();

    @Test
    public void shouldWaitForRightResponse() {
        DummyLog logger = new DummyLog();
        WaitingWorker waitingWorker = WaitingWorker
                .builder()
                .tester(getHttpTester(new IrregularContentProvider()))
                .maxWaiting(3000)
                .pollingInterval(1000)
                .logger(logger)
                .build();
        Assert.assertTrue(waitingWorker.doWaiting());

        for (int i = 3; i >= 1; i--) {
            Assert.assertTrue(logger.getMessages().contains("max " + i + " sec. remaining"));
        }
        Assert.assertTrue(logger.getMessages().contains("Succeeded after 2 sec."));
    }

    @Test
    public void shouldAbandonIfNoRightResponse() {
        DummyLog logger = new DummyLog();
        WaitingWorker waitingWorker = WaitingWorker
                .builder()
                .tester(getHttpTester(new FailingContentProvider()))
                .maxWaiting(3500)
                .pollingInterval(1000)
                .logger(logger)
                .build();
        Assert.assertFalse(waitingWorker.doWaiting());
        for (int i = 3; i >= 1; i--) {
            Assert.assertTrue(logger.getMessages().contains("max " + i + " sec. remaining"));
        }
        Assert.assertTrue(logger.getMessages().contains("After 3 sec., we haven't received the expected response."));
    }

    private static HttpTester getHttpTester(ContentProvider contentProvider) {
        return HttpTester
                .builder()
                .contentProvider(contentProvider)
                .logger(SILENT_LOG)
                .mustContainHtml("body .root .column .cell")
                .build();
    }

    private static class IrregularContentProvider extends DummyContentProvider {
        private final AtomicInteger callsCount = new AtomicInteger();

        @Override
        public String getContent() {
            if (callsCount.incrementAndGet() < 3) {
                return "Page not found";
            }
            return super.getContent();
        }
    }

    private static class FailingContentProvider extends DummyContentProvider {
        @Override
        public String getContent() {
            return StringUtils.EMPTY;
        }
    }
}
