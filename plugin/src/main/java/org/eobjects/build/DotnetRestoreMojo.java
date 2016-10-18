package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "restore", defaultPhase = LifecyclePhase.VALIDATE)
public class DotnetRestoreMojo extends AbstractDotnetMojo {
    
    @Parameter(property = "dotnet.restore.enabled", required = false, defaultValue = "true")
    private boolean restoreEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!restoreEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            helper.executeCommand(subDirectory, "dotnet restore");
        }
    }
}
