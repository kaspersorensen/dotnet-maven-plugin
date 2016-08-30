package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "build", defaultPhase = LifecyclePhase.COMPILE)
public class DotnetBuildMojo extends AbstractMojo {

    private final DotnetHelper dotnetHelper = DotnetHelper.get();

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (File subDirectory : dotnetHelper.getProjectDirectories()) {
            if (!new File(subDirectory, "project.lock.json").exists()) {
                dotnetHelper.executeCommand(subDirectory, "dotnet restore");
            }
            dotnetHelper.executeCommand(subDirectory, "dotnet build");
        }
    }
}
