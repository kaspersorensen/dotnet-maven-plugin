package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "nuget-add", defaultPhase = LifecyclePhase.INSTALL)
public class NugetAddMojo extends AbstractDotnetMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        final PluginHelper helper = getPluginHelper();
        if (!helper.isNugetAvailable()) {
            getLog().warn("The [nuget] command is not available on path, skipping");
            return;
        }

        for (File subDirectory : helper.getProjectDirectories()) {
            try {
                final String targetPath = System.getProperty("user.home") + "/.nuget/packages";
                final File nugetPackage = helper.getNugetPackage(subDirectory);
                final String nugetPackagePath = nugetPackage.getCanonicalPath();
                helper.executeCommand(subDirectory, "nuget", "add", nugetPackagePath, "-Source", targetPath);
            } catch (Exception e) {
                throw new MojoFailureException("Command [nuget add] failed!", e);
            }
        }
    }
}
