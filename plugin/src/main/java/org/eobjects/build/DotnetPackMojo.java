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
            helper.executeCommand(subDirectory, "dotnet", "pack", "-o", output, "-c", helper.getBuildConfiguration(),
                    "--version-suffix", version);
            projectHelper.attachArtifact(project, "project.json", new File(subDirectory, "project.json"));
        }
    }
}
