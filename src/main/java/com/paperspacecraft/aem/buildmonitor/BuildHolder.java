package com.paperspacecraft.aem.buildmonitor;

import com.paperspacecraft.aem.buildmonitor.http.HttpTester;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


@Mojo(name = "wait", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true, aggregator = true)
public class BuildHolder extends BuildMonitorBase {

    @Override
    public void execute() {
        if (!isTopLevelOrLast()) {
            getLog().info(String.format("Skipping module '%s'", getSession().getCurrentProject().getName()));
            return;
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

    void execute(HttpTester httpTester) {
        WaitingWorker waitingWorker = WaitingWorker
                .builder()
                .tester(httpTester)
                .maxWaiting(toMilliseconds(getMaxWaiting()))
                .pollingInterval(toMilliseconds(getPollingInterval()))
                .logger(getLog())
                .build();
        waitingWorker.doWaiting();
    }

    private static int toMilliseconds(String value) {
        return Integer.parseInt(value) * 1000;
    }
}
