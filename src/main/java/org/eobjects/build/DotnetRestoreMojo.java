package org.eobjects.build;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "restore", defaultPhase = LifecyclePhase.VALIDATE)
public class DotnetRestoreMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("!!! --- I AM THE DOTNET [RESTORE] MOJO --- !!!");
    }
}
