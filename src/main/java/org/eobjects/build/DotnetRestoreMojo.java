package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "restore", defaultPhase = LifecyclePhase.VALIDATE)
public class DotnetRestoreMojo extends AbstractMojo {

    @Parameter(defaultValue = PluginHelper.PROPERTY_BASEDIR, readonly = true)
    private File basedir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final PluginHelper helper = PluginHelper.get(basedir);
        for (File subDirectory : helper.getProjectDirectories()) {
            helper.executeCommand(subDirectory, "dotnet restore");
        }
    }
}
