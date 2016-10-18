package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "build", defaultPhase = LifecyclePhase.COMPILE)
public class DotnetBuildMojo extends AbstractDotnetMojo {

    @Parameter(property = "dotnet.build.enabled", required = false, defaultValue = "true")
    private boolean buildEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!buildEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            if (!new File(subDirectory, "project.lock.json").exists()) {
                // restore first if needed
                helper.executeCommand(subDirectory, "dotnet", "restore");
            }
            helper.executeCommand(subDirectory, "dotnet", "build", "-c", helper.getBuildConfiguration());
        }
    }
}
