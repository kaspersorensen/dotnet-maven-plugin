package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "nuget-add", defaultPhase = LifecyclePhase.INSTALL)
public class NugetAddMojo extends AbstractMojo {

    @Parameter(defaultValue = PluginHelper.PROPERTY_BASEDIR, readonly = true)
    private File basedir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final PluginHelper helper = PluginHelper.get(basedir);
        if (!helper.isNugetAvailable()) {
            getLog().warn("The [nuget] command is not available on path, skipping");
            return;
        }

        for (File subDirectory : helper.getProjectDirectories()) {
            try {
                final String targetPath = System.getProperty("user.home") + "/.nuget/packages";
                final File nugetPackage = helper.getNugetPackage(subDirectory);
                final String nugetPackagePath = nugetPackage.getCanonicalPath();
                helper.executeCommand(subDirectory, "nuget", "add", nugetPackagePath, "-Source", targetPath);
            } catch (Exception e) {
                throw new MojoFailureException("Command [nuget add] failed!", e);
            }
        }
    }
}
