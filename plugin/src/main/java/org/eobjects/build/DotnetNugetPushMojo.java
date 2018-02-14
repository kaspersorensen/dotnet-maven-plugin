package org.eobjects.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "nuget-push", defaultPhase = LifecyclePhase.DEPLOY)
public class DotnetNugetPushMojo extends AbstractDotnetMojo {

    @Parameter(property = "nuget.push.source", required = false)
    private String repository;
    
    @Parameter(property = "nuget.push.enabled", required = false, defaultValue = "true")
    private boolean nugetPushEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!nugetPushEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();
        if (repository == null || repository.isEmpty()) {
            getLog().info("No 'nuget-repository' configured, skipping");
            return;
        }

        for (File subDirectory : helper.getProjectDirectories()) {
            try {
                final File nugetPackage = helper.getNugetPackage(subDirectory);
                final String repositoryKey = helper.getRepositoryKey();
                final String nugetPackagePath = nugetPackage.getCanonicalPath();
                
                final List<String> cmd = new ArrayList<>(Arrays.asList("dotnet", "nuget", "push"));
                cmd.add(nugetPackagePath);
                cmd.add("-s");
                cmd.add(repository);
                if (repositoryKey != null && !repositoryKey.isEmpty()) {
                  cmd.add("-k");
                  cmd.add(repositoryKey);
                }
                helper.executeCommand(subDirectory, cmd.toArray((new String[cmd.size()])));
                
                
            } catch (Exception e) {
                throw new MojoFailureException("Command [dotnet nuget push] failed!", e);
            }
        }
    }
}
