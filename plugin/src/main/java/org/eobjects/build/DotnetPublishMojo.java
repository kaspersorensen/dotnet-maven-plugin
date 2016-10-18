package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "publish", defaultPhase = LifecyclePhase.VERIFY)
public class DotnetPublishMojo extends AbstractDotnetMojo {

    @Parameter(property = "dotnet.publish.enabled", required = false, defaultValue = "true")
    private boolean publishEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!publishEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }
        
        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            helper.executeCommand(subDirectory, "dotnet", "publish", "-c", helper.getBuildConfiguration());
        }
    }
}
