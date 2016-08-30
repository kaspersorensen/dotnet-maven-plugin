package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "nuget-add", defaultPhase = LifecyclePhase.INSTALL)
public class NugetAddMojo extends AbstractMojo {

    private final DotnetHelper dotnetHelper = DotnetHelper.get();

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!dotnetHelper.isNugetAvailable()) {
            getLog().warn("The [nuget] command is not available on path, skipping");
            return;
        }
        
        for (File subDirectory : dotnetHelper.getProjectDirectories()) {
            try {
                final String targetPath = System.getProperty("user.home") + "/.nuget/packages";
                final File nugetPackage = dotnetHelper.getNugetPackage(subDirectory);
                final String nugetPackagePath = nugetPackage.getCanonicalPath();
                dotnetHelper.executeCommand(subDirectory, "nuget", "add", nugetPackagePath, "-Source", targetPath);
            } catch (Exception e) {
                throw new MojoFailureException("Command [nuget add] failed!", e);
            }
        }
    }
}
