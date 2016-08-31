package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "nuget-push", defaultPhase = LifecyclePhase.DEPLOY)
public class NugetPushMojo extends AbstractMojo {

    @Parameter(property = "nuget-push.repository", alias = "nuget-repository", required = true)
    private String repository;
    
    @Parameter( defaultValue = PluginHelper.PROPERTY_BASEDIR, readonly = true )
    private File basedir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final PluginHelper helper = PluginHelper.get(basedir);
        if (repository == null || repository.isEmpty()) {
            getLog().info("No 'nuget-repository' configured, skipping");
            return;
        }

        if (!helper.isNugetAvailable()) {
            getLog().warn("The [nuget] command is not available on path, skipping");
            return;
        }

        for (File subDirectory : helper.getProjectDirectories()) {
            try {
                final File nugetPackage = helper.getNugetPackage(subDirectory);
                final String nugetPackagePath = nugetPackage.getCanonicalPath();
                helper.executeCommand(subDirectory, "nuget", "push", nugetPackagePath, "-Source", repository);
            } catch (Exception e) {
                throw new MojoFailureException("Command [nuget push] failed!", e);
            }
        }
    }
}
