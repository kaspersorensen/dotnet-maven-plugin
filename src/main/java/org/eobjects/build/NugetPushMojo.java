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

    private final DotnetHelper dotnetHelper = DotnetHelper.get();

    @Parameter(property = "nuget-push.repository", alias = "nuget-repository", required = true)
    private String repository;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (repository == null || repository.isEmpty()) {
            getLog().info("No 'nuget-repository' configured, skipping");
            return;
        }

        if (!dotnetHelper.isNugetAvailable()) {
            getLog().warn("The [nuget] command is not available on path, skipping");
            return;
        }

        for (File subDirectory : dotnetHelper.getProjectDirectories()) {
            try {
                final File nugetPackage = dotnetHelper.getNugetPackage(subDirectory);
                final String nugetPackagePath = nugetPackage.getCanonicalPath();
                dotnetHelper.executeCommand(subDirectory, "nuget", "push", nugetPackagePath, "-Source", repository);
            } catch (Exception e) {
                throw new MojoFailureException("Command [nuget push] failed!", e);
            }
        }
    }
}
