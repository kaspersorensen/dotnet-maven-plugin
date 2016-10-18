package org.eobjects.build;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST)
public class DotnetTestMojo extends AbstractDotnetTestMojo {

    @Parameter(property = "dotnet.test.enabled", required = false, defaultValue = "true")
    private boolean testEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!testEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        executeInternal();
    }
}
