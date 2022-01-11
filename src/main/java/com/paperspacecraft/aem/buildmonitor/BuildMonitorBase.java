package com.paperspacecraft.aem.buildmonitor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter(AccessLevel.PACKAGE)
abstract class BuildMonitorBase extends AbstractMojo {

    private static final String DEFAULT_ENDPOINT = "http://localhost:4502";
    private static final String DEFAULT_LOGIN = "admin";
    private static final String DEFAULT_PASSWORD = DEFAULT_LOGIN;

    private static final String DEFAULT_POLLING_INTERVAL = "3";
    private static final String DEFAULT_MAX_WAITING = "120";

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    @Setter(value = AccessLevel.PACKAGE)
    private MavenSession session;

    @Parameter(defaultValue = "${buildmon.endpoint}", readonly = true)
    private String endpoint;

    @Parameter(defaultValue = "${buildmon.login}", alias = "login", readonly = true)
    @Getter(AccessLevel.PACKAGE)
    private String login;

    @Parameter(defaultValue = "${buildmon.password}", readonly = true)
    @Getter(AccessLevel.PACKAGE)
    private String password;

    @Parameter(defaultValue = "${buildmon.pollingInterval}", readonly = true)
    @Getter(AccessLevel.PACKAGE)
    private String pollingInterval;

    @Parameter(defaultValue = "${buildmon.maxWaiting}", readonly = true)
    @Getter(AccessLevel.PACKAGE)
    private String maxWaiting;

    @Parameter(defaultValue = "${buildmon.mustContainHtml}", readonly = true)
    private String mustContainHtml;

    @Parameter(defaultValue = "${buildmon.mustContainText}", readonly = true)
    private String mustContainText;

    boolean isTopLevelOrLast() {
        List<MavenProject> sortedProjects = session
                .getProjectDependencyGraph()
                .getSortedProjects();
        LinkedList<MavenProject> filteredProjects = sortedProjects
                .stream()
                .filter(project -> StringUtils.equals(project.getPackaging(), "content-package"))
                .collect(Collectors.toCollection(LinkedList::new));
        boolean isTopLevel = sortedProjects.isEmpty() || sortedProjects.get(0).equals(session.getCurrentProject().getExecutionProject());
        boolean isLast = filteredProjects.isEmpty() || filteredProjects.getLast().equals(session.getCurrentProject().getExecutionProject());
        return isTopLevel || isLast;
    }

    void verifySettings() {
        if (StringUtils.isBlank(endpoint)) {
            getLog().info(String.format("Using default endpoint '%s'", DEFAULT_ENDPOINT));
            endpoint = DEFAULT_ENDPOINT;
        } else {
            getLog().info(String.format("Using endpoint '%s'", endpoint));
        }

        if (StringUtils.isBlank(login)) {
            getLog().info(String.format("Using default user login '%s'", DEFAULT_LOGIN));
            login = DEFAULT_LOGIN;
        } else {
            getLog().info(String.format("Using user login '%s'", login));
        }

        if (StringUtils.isBlank(password)) {
            getLog().info("Using default password");
            password = DEFAULT_PASSWORD;
        }

        if (!StringUtils.isNumeric(pollingInterval)) {
            getLog().info(String.format("Using default polling interval %s sec.", DEFAULT_POLLING_INTERVAL));
            pollingInterval = DEFAULT_POLLING_INTERVAL;
        } else {
            getLog().info(String.format("Using polling interval %s sec.", pollingInterval));
        }

        if (!StringUtils.isNumeric(maxWaiting) || Integer.parseInt(maxWaiting) <= Integer.parseInt(pollingInterval)) {
            getLog().info(String.format("Using default \"max waiting\" interval %s sec.", DEFAULT_MAX_WAITING));
            maxWaiting = DEFAULT_MAX_WAITING;
        } else {
            getLog().info(String.format("Using max waiting interval %s sec.", maxWaiting));
        }

        if (StringUtils.isNotEmpty(mustContainHtml)) {
            getLog().info(String.format("Using HTML matching '%s'", mustContainHtml));
        } else if (StringUtils.isNotEmpty(mustContainText)) {
            getLog().info(String.format("Using text matching '%s'", mustContainText));
        }
    }
}
