package com.paperspacecraft.aem.buildmonitor;

import com.paperspacecraft.aem.buildmonitor.http.HttpTester;
import com.paperspacecraft.aem.buildmonitor.util.DummyContentProvider;
import com.paperspacecraft.aem.buildmonitor.util.DummyLog;
import lombok.Getter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class BuildVerifierTest extends BuildMonitorTestBase {

    @Rule
    @Getter
    public MojoRule mojoRule = new MojoRule();

    @Test
    public void shouldProceedIfConditionMet() throws Exception {
        DummyLog logger = new DummyLog();
        BuildVerifier child1 = getBuildVerifier("Child 1 Package");
        Assert.assertNotNull(child1);

        child1.setLog(logger);
        child1.verifySettings();
        
        HttpTester httpTester = HttpTester
                .builder()
                .contentProvider(new DummyContentProvider())
                .logger(logger)
                .mustContainText("Hello world")
                .build();
        child1.execute(httpTester);

        Assert.assertTrue(logger.getMessages().contains("Verified."));
    }


    @Test
    public void shouldThrowIfConditionNotMet() throws Exception {
        BuildVerifier child1 = getBuildVerifier("Child 1 Package");
        Assert.assertNotNull(child1);

        child1.verifySettings();
        HttpTester httpTester = HttpTester
                .builder()
                .contentProvider(new DummyContentProvider())
                .logger(child1.getLog())
                .mustContainText("Goodbye world")
                .build();

        String exceptionMessage = null;
        try {
            child1.execute(httpTester);
        } catch (MojoExecutionException e) {
            exceptionMessage = e.getMessage();
        }
        Assert.assertEquals("Verification failed.", exceptionMessage);
    }

}
