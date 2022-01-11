package com.paperspacecraft.aem.buildmonitor;

import com.paperspacecraft.aem.buildmonitor.http.HttpTester;
import com.paperspacecraft.aem.buildmonitor.util.DummyContentProvider;
import com.paperspacecraft.aem.buildmonitor.util.DummyLog;
import lombok.Getter;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class BuildHolderTest extends BuildMonitorTestBase {

    private static final String CHILD_PACKAGE = "Child 1 Package";
    private static final String GRANDCHILD_PACKAGE = "Grandchild 0 Package";

    @Rule
    @Getter
    public MojoRule mojoRule = new MojoRule();

    @Test
    public void shouldDetectLastProjectInReactor() throws Exception {
        BuildHolder child0 = getBuildHolder("Child 0");
        Assert.assertNotNull(child0);
        Assert.assertFalse(child0.isTopLevelOrLast());
        child0.execute();

        BuildHolder child1 = getBuildHolder(CHILD_PACKAGE);
        Assert.assertNotNull(child1);
        Assert.assertFalse(child1.isTopLevelOrLast());

        BuildHolder grandchild0 = getBuildHolder(GRANDCHILD_PACKAGE);
        Assert.assertNotNull(grandchild0);
        Assert.assertTrue(grandchild0.isTopLevelOrLast());
    }

    @Test
    public void shouldParseSettings() throws Exception {
        BuildHolder child1 = getBuildHolder(CHILD_PACKAGE);
        Assert.assertNotNull(child1);
        child1.verifySettings();
        Assert.assertEquals("http://localhost:4502", child1.getEndpoint());
        Assert.assertEquals("hello", child1.getLogin());
        Assert.assertEquals("world", child1.getPassword());
        Assert.assertEquals("25", child1.getPollingInterval());

        BuildHolder grandchild0 = getBuildHolder(GRANDCHILD_PACKAGE);
        Assert.assertNotNull(grandchild0);
        grandchild0.verifySettings();
        Assert.assertEquals("geronimo", grandchild0.getLogin());
        Assert.assertEquals("3", grandchild0.getPollingInterval());
        Assert.assertEquals("60", grandchild0.getMaxWaiting());
    }

    @Test
    public void shouldWaitAndProceed() throws Exception {
        DummyLog logger = new DummyLog();
        BuildHolder child1 = getBuildHolder(CHILD_PACKAGE);
        Assert.assertNotNull(child1);

        child1.setLog(logger);
        HttpTester httpTester = HttpTester
                .builder()
                .contentProvider(new DummyContentProvider())
                .logger(logger)
                .mustContainText("Hello world")
                .build();
        child1.verifySettings();
        child1.execute(httpTester);

        Assert.assertTrue(logger.getMessages().contains("Success."));
    }
}
