package com.paperspacecraft.aem.buildmonitor;

import com.paperspacecraft.aem.buildmonitor.http.HttpTester;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "verify", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class BuildVerifier extends BuildMonitorBase {

    @Override
    public void execute() throws MojoExecutionException {
        if (!isTopLevelOrLast()) {
            getLog().debug(String.format("Skipping module '%s'", getSession().getCurrentProject().getName()));
        }
        verifySettings();
        HttpTester httpTester = HttpTester
                .builder()
                .endpoint(getEndpoint())
                .userName(getLogin())
                .password(getPassword())
                .mustContainHtml(getMustContainHtml())
                .mustContainText(getMustContainText())
                .logger(getLog())
                .build();
        execute(httpTester);
    }

    void execute(HttpTester httpTester) throws MojoExecutionException {
        if (!httpTester.test()) {
            throw new MojoExecutionException("Verification failed.");
        }
        getLog().info("Verified.");
    }
}
