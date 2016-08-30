package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "publish", defaultPhase = LifecyclePhase.VERIFY)
public class DotnetPublishMojo extends AbstractMojo {

    private final DotnetHelper dotnetHelper = DotnetHelper.get();

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (File subDirectory : dotnetHelper.getProjectDirectories()) {
            dotnetHelper.executeCommand(subDirectory, "dotnet", "publish","-c", dotnetHelper.getBuildConfiguration());
        }
    }
}
