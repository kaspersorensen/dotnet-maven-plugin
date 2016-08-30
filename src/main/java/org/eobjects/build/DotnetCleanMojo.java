package org.eobjects.build;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class DotnetCleanMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("!!! --- I AM THE DOTNET [CLEAN] MOJO --- !!!");
    }
}
