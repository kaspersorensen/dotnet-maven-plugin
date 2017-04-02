package org.eobjects.build;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "update-versions")
public class DotnetVersionUpdateMojo extends AbstractDotnetMojo {

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String version;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final File[] projectDirectories = getPluginHelper().getProjectDirectories(false);
        for (File directory : projectDirectories) {
            final DotnetProjectFile projectFile = getPluginHelper().getProjectFile(directory);
            try {
                updateVersion(projectFile);
            } catch (Exception e) {
                throw new MojoExecutionException("Failed to update version in file: " + projectFile.getFile(), e);
            }
        }
    }

    private void updateVersion(DotnetProjectFile projectFile) throws Exception {
        boolean updatesMade = false;

        final String projectJsonVersion = projectFile.getVersion();
        if (needsUpdating(projectJsonVersion, false)) {
            projectFile.setVersion(version);
            getLog().info("Updating module version to: " + version);
            updatesMade = true;
        }

        final List<DotnetProjectDependency> dependencies = projectFile.getDependencies();
        for (DotnetProjectDependency dependency : dependencies) {
            if (needsUpdating(dependency.getVersion())) {
                getLog().info("Updating dependency '" + dependency.getName() + "' to version: " + version);
                projectFile.setDependencyVersion(dependency, version);
                updatesMade = true;
            }
        }

        if (updatesMade) {
            getLog().info("Writing updated file (version '" + version + "'): " + projectJsonVersion);
            projectFile.saveChanges();
        }
    }

    private boolean needsUpdating(String aVersion) {
        return needsUpdating(aVersion, true);
    }

    private boolean needsUpdating(String aVersion, boolean onlySnapshots) {
        if (aVersion == null) {
            return false;
        }
        if (onlySnapshots && !aVersion.toUpperCase().endsWith("-SNAPSHOT")) {
            return false;
        }
        return !version.equals(aVersion);
    }
}
