package com.paperspacecraft.aem.buildmonitor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.graph.DefaultProjectDependencyGraph;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.Before;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

abstract class BuildMonitorTestBase {
    private static final String ROOT_DIRECTORY = "src/test/resources/com/paperspacecraft/aem/buildmonitor/pom";

    abstract MojoRule getMojoRule();

    private List<MavenProject> projects;

    @Before
    public void setUp() throws Exception {
        this.projects = initProjects();
    }

    BuildHolder getBuildHolder(String projectName) throws Exception {
        return getBuildMonitor(projectName, "wait", BuildHolder.class);
    }

    @SuppressWarnings("SameParameterValue")
    BuildVerifier getBuildVerifier(String projectName) throws Exception {
        return getBuildMonitor(projectName, "verify", BuildVerifier.class);
    }

    private <T extends BuildMonitorBase> T getBuildMonitor(String projectName, String goal, Class<T> type) throws Exception {
        MavenProject currentProject = projects
                .stream()
                .filter(project -> project.getName().equals(projectName))
                .findFirst()
                .orElse(null);
        if (currentProject == null) {
            return null;
        }
        MavenSession session = getMojoRule().newMavenSession(currentProject);
        session.setProjectDependencyGraph(new DefaultProjectDependencyGraph(projects));

        BuildMonitorBase result = (BuildMonitorBase) getMojoRule().lookupConfiguredMojo(currentProject, goal);
        result.setSession(session);
        return type.cast(result);
    }

    private List<MavenProject> initProjects() throws Exception {
        File rootDirectory = new File(ROOT_DIRECTORY);
        MavenProject rootProject = getMojoRule().readMavenProject(rootDirectory);

        projects = new ArrayList<>();
        initProjects(rootDirectory, projects);
        projects.add(rootProject);
        return projects;
    }

    private void initProjects(File rootDirectory, List<MavenProject> accumulator) throws Exception {
        File[] children = rootDirectory.listFiles();
        if (ArrayUtils.isEmpty(children)) {
            return;
        }
        assert children != null;
        for (File child : children) {
            if (!child.isDirectory()) {
                continue;
            }
            MavenProject newProject = getMojoRule().readMavenProject(child);
            if (newProject.getName().contains("Package")) {
                newProject.setPackaging("content-package");
            }
            accumulator.add(newProject);
            initProjects(child, accumulator);
        }
    }
}
