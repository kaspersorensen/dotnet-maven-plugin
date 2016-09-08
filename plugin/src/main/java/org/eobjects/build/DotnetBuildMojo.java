package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "build", defaultPhase = LifecyclePhase.COMPILE)
public class DotnetBuildMojo extends AbstractDotnetMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            if (!new File(subDirectory, "project.lock.json").exists()) {
                helper.executeCommand(subDirectory, "dotnet", "-c", helper.getBuildConfiguration(), "restore");
            }
            helper.executeCommand(subDirectory, "dotnet build");
        }
    }
}
