package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

@Mojo(name = "pack", defaultPhase = LifecyclePhase.PACKAGE)
public class DotnetPackMojo extends AbstractDotnetMojo {

    @Parameter(defaultValue = "${project.version}", readonly = true)
    String version;

    @Component
    MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject project;

    @Parameter(property = "dotnet.pack.enabled", required = false, defaultValue = "true")
    boolean packEnabled;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!packEnabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        final PluginHelper helper = getPluginHelper();

        for (File subDirectory : helper.getProjectDirectories()) {
            final String output = helper.getNugetPackageDir(subDirectory).getPath();
            
            // Optionally target a particular framework to pack for. Linux-based dotnet cannot build/pack NET45 easily.
            final String optFramework = helper.getBuildTargetFramework() == "" ? "": "/p:TargetFrameworks=" + helper.getBuildTargetFramework(); 

            helper.executeCommand(subDirectory, "dotnet", "pack", "-o", output, "-c", helper.getBuildConfiguration(), optFramework);

            final DotnetProjectFile projectFile = getPluginHelper().getProjectFile(subDirectory);

            projectHelper.attachArtifact(project, projectFile.getFile().getName(), projectFile.getFile());
        }
    }
}
