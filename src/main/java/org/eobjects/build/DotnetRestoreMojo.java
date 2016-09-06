package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "restore", defaultPhase = LifecyclePhase.VALIDATE)
public class DotnetRestoreMojo extends AbstractDotnetMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            helper.executeCommand(subDirectory, "dotnet restore");
        }
    }
}
